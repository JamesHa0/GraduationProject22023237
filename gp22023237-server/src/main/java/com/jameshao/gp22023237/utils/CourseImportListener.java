package com.jameshao.gp22023237.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jameshao.gp22023237.DTO.CourseImportDTO;
import com.jameshao.gp22023237.DTO.CourseImportResultDTO;
import com.jameshao.gp22023237.common.error.ImportErrorCode;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.service.TeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 课程导入Excel监听器
 */
@Slf4j
public class CourseImportListener implements ReadListener<CourseImportDTO> {

    private final int batchCount;

    /**
     * 缓存的数据
     */
    private List<RowData> cachedDataList;

    private final CourseService courseService;
    private final TeacherService teacherService;
    private final CourseMapper courseMapper;

    private final List<CourseImportResultDTO.FailDetail> failDetails = new ArrayList<>();
    private int successCount = 0;
    private int totalCount = 0;

    // 文件内判重，避免同一批导入重复课程号
    private final Set<String> fileCourseNoSet = new HashSet<>();

    public CourseImportListener(CourseService courseService,
                                TeacherService teacherService,
                                CourseMapper courseMapper,
                                int batchCount) {
        this.courseService = courseService;
        this.teacherService = teacherService;
        this.courseMapper = courseMapper;
        this.batchCount = batchCount;
        this.cachedDataList = ListUtils.newArrayListWithExpectedSize(batchCount);
    }

    @Override
    public void invoke(CourseImportDTO data, AnalysisContext context) {
        totalCount++;
        int row = context.readRowHolder().getRowIndex() + 1;
        cachedDataList.add(new RowData(row, data));
        if (cachedDataList.size() >= batchCount) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(batchCount);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("课程导入解析完成，总记录:{}，成功:{}，失败:{}", totalCount, successCount, failDetails.size());
    }

    /**
     * 批量保存并校验
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveData() {
        if (cachedDataList.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();

        Set<String> courseNos = new HashSet<>();
        for (RowData rowData : cachedDataList) {
            if (rowData.dto.getCourseNo() != null && !rowData.dto.getCourseNo().trim().isEmpty()) {
                courseNos.add(rowData.dto.getCourseNo().trim());
            }
        }

        Map<String, Boolean> existingCourseNoMap = preloadExistingCourseNoMap(courseNos);

        List<Course> validCourses = new ArrayList<>();

        for (RowData rowData : cachedDataList) {
            CourseImportResultDTO.FailDetail failDetail = validateData(rowData, existingCourseNoMap);
            if (failDetail != null) {
                failDetails.add(failDetail);
                continue;
            }
            try {
                Course course = convertToCourse(rowData.dto);
                validCourses.add(course);
            } catch (Exception e) {
                log.error("第{}行数据转换失败", rowData.row, e);
                failDetails.add(toFailDetail(rowData.row, rowData.dto.getCourseNo(), ImportErrorCode.SYSTEM_ERROR,
                        "数据转换失败: " + e.getMessage()));
            }
        }

        if (!validCourses.isEmpty()) {
            courseService.saveBatch(validCourses, batchCount);
            successCount += validCourses.size();
        }

        long cost = System.currentTimeMillis() - start;
        log.info("课程导入批次处理完成，批次大小:{}，成功:{}，失败:{}，耗时:{}ms",
                cachedDataList.size(), validCourses.size(), cachedDataList.size() - validCourses.size(), cost);
    }

    private Map<String, Boolean> preloadExistingCourseNoMap(Set<String> courseNos) {
        Map<String, Boolean> map = new HashMap<>();
        if (courseNos.isEmpty()) {
            return map;
        }
        List<Course> existingCourses = courseMapper.listByCourseNos(new ArrayList<>(courseNos));
        for (Course course : existingCourses) {
            if (course.getCourseNo() != null) {
                map.put(course.getCourseNo(), true);
            }
        }
        return map;
    }

    /**
     * 校验数据
     */
    private CourseImportResultDTO.FailDetail validateData(RowData rowData, Map<String, Boolean> existingCourseNoMap) {
        CourseImportDTO data = rowData.dto;

        // 课程编号必填
        if (data.getCourseNo() == null || data.getCourseNo().trim().isEmpty()) {
            return toFailDetail(rowData.row, data.getCourseNo(), ImportErrorCode.COURSE_NO_REQUIRED);
        }
        String courseNo = data.getCourseNo().trim();

        // 课程编号长度检查
        if (courseNo.length() > 20) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.COURSE_NO_REQUIRED, "课程编号长度不能超过20");
        }

        // 课程编号唯一性检查（系统已存在）
        if (Boolean.TRUE.equals(existingCourseNoMap.get(courseNo))) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.COURSE_NO_DUPLICATE_DB);
        }

        // 文件内重复课程号检查
        if (fileCourseNoSet.contains(courseNo)) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.COURSE_NO_DUPLICATE_FILE);
        }

        // 课程名称必填
        if (data.getName() == null || data.getName().trim().isEmpty()) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.NAME_REQUIRED);
        }

        // 课程名称长度检查
        if (data.getName().length() > 100) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.NAME_REQUIRED, "课程名称长度不能超过100");
        }

        // 学分必填和范围
        if (data.getCredit() == null || data.getCredit() < 0.5 || data.getCredit() > 10) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.CREDIT_RANGE_INVALID);
        }

        // 学时必填和范围
        if (data.getHours() == null || data.getHours() < 1 || data.getHours() > 200) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.HOURS_RANGE_INVALID);
        }

        // 学期必填
        if (data.getSemester() == null || data.getSemester().trim().isEmpty()) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.SEMESTER_REQUIRED);
        }

        if (data.getSemester().length() > 20) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.SEMESTER_REQUIRED, "学期长度不能超过20");
        }

        // 学年必填
        if (data.getYear() == null) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.SEMESTER_REQUIRED, "学年不能为空");
        }

        // 状态校验
        if (data.getStatus() != null && (data.getStatus() < 0 || data.getStatus() > 2)) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.STATUS_INVALID);
        }

        // 教材校验
        if (data.getTextbook() != null && data.getTextbook() != 0 && data.getTextbook() != 1) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.SYSTEM_ERROR, "教材必须是0-否或1-是");
        }

        // 外年级选课校验
        if (data.getExternalSelection() != null && data.getExternalSelection() != 0 && data.getExternalSelection() != 1) {
            return toFailDetail(rowData.row, courseNo, ImportErrorCode.SYSTEM_ERROR, "外年级选课必须是0-否或1-是");
        }

        fileCourseNoSet.add(courseNo);
        return null;
    }

    /**
     * 转换DTO为Course实体
     */
    private Course convertToCourse(CourseImportDTO dto) {
        Course course = new Course();
        course.setCourseNo(dto.getCourseNo().trim());
        course.setName(dto.getName().trim());
        course.setCredit(dto.getCredit());
        course.setHours(dto.getHours());
        course.setSemester(dto.getSemester());
        course.setYear(dto.getYear());

        // 设置默认值
        course.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        course.setTextbook(dto.getTextbook() != null ? dto.getTextbook() : 0);
        course.setExternalSelection(dto.getExternalSelection() != null ? dto.getExternalSelection() : 0);

        // 设置可选字段
        course.setMaxCredits(dto.getMaxCredits());
        course.setDescription(dto.getDescription());
        course.setStudyNature(dto.getStudyNature());
        course.setRemark(dto.getRemark());

        // 设置时间
        Date now = new Date();
        course.setCreateTime(now);
        course.setUpdateTime(now);

        return course;
    }

    private CourseImportResultDTO.FailDetail toFailDetail(int row, String courseNo, ImportErrorCode code) {
        return new CourseImportResultDTO.FailDetail(
                row,
                courseNo,
                code.getMessage(),
                code.getCode(),
                code.getField(),
                code.getSuggestion(),
                code.getRule()
        );
    }

    private CourseImportResultDTO.FailDetail toFailDetail(int row, String courseNo, ImportErrorCode code, String customReason) {
        return new CourseImportResultDTO.FailDetail(
                row,
                courseNo,
                customReason,
                code.getCode(),
                code.getField(),
                code.getSuggestion(),
                code.getRule()
        );
    }

    /**
     * 获取导入结果
     */
    public CourseImportResultDTO getResult() {
        return new CourseImportResultDTO(
                totalCount,
                successCount,
                failDetails.size(),
                failDetails
        );
    }

    private static class RowData {
        private final int row;
        private final CourseImportDTO dto;

        private RowData(int row, CourseImportDTO dto) {
            this.row = row;
            this.dto = dto;
        }
    }
}
