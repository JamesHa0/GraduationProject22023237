package com.jameshao.gp22023237.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jameshao.gp22023237.DTO.CourseImportDTO;
import com.jameshao.gp22023237.DTO.CourseImportResultDTO;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.CourseService;
import com.jameshao.gp22023237.service.TeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 课程导入Excel监听器
 */
@Slf4j
public class CourseImportListener implements ReadListener<CourseImportDTO> {

    /**
     * 每隔100条存储数据库
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 缓存的数据
     */
    private List<CourseImportDTO> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    private final CourseService courseService;
    private final TeacherService teacherService;

    private final List<CourseImportResultDTO.FailDetail> failDetails = new ArrayList<>();
    private int successCount = 0;
    private int totalCount = 0;
    private int currentRow = 1; // 表头是第1行，数据从第2行开始

    // 用于存储需要保存的课程
    private final List<Course> coursesToSave = new ArrayList<>();

    // 待定教师ID，由外部传入
    private Long defaultTeacherId;

    public CourseImportListener(CourseService courseService, TeacherService teacherService, Long defaultTeacherId) {
        this.courseService = courseService;
        this.teacherService = teacherService;
        this.defaultTeacherId = defaultTeacherId;
    }

    @Override
    public void invoke(CourseImportDTO data, AnalysisContext context) {
        currentRow++;
        totalCount++;
        log.info("解析到第{}条数据: {}", currentRow, data);

        // 校验数据
        String error = validateData(data);
        if (error != null) {
            failDetails.add(new CourseImportResultDTO.FailDetail(currentRow, data.getCourseNo(), error));
            return;
        }

        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("所有数据解析完成！");
    }

    /**
     * 校验数据
     */
    private String validateData(CourseImportDTO data) {
        // 课程编号必填
        if (data.getCourseNo() == null || data.getCourseNo().trim().isEmpty()) {
            return "课程编号不能为空";
        }
        // 课程编号长度检查
        if (data.getCourseNo().length() > 20) {
            return "课程编号长度不能超过20";
        }
        // 课程编号唯一性检查
        QueryWrapper<Course> courseWrapper = new QueryWrapper<>();
        courseWrapper.eq("course_no", data.getCourseNo());
        if (courseService.getOne(courseWrapper) != null) {
            return "课程编号已存在";
        }

        // 课程名称必填
        if (data.getName() == null || data.getName().trim().isEmpty()) {
            return "课程名称不能为空";
        }
        // 课程名称长度检查
        if (data.getName().length() > 100) {
            return "课程名称长度不能超过100";
        }

        // 学分必填
        if (data.getCredit() == null) {
            return "学分不能为空";
        }
        if (data.getCredit() < 0.5 || data.getCredit() > 10) {
            return "学分必须在0.5-10之间";
        }

        // 学时必填
        if (data.getHours() == null) {
            return "学时不能为空";
        }
        if (data.getHours() < 1 || data.getHours() > 200) {
            return "学时必须在1-200之间";
        }

        // 学期必填
        if (data.getSemester() == null || data.getSemester().trim().isEmpty()) {
            return "学期不能为空";
        }
        if (data.getSemester().length() > 20) {
            return "学期长度不能超过20";
        }

        // 学年必填
        if (data.getYear() == null) {
            return "学年不能为空";
        }

        // 星期几校验
        if (data.getDayOfWeek() != null && (data.getDayOfWeek() < 1 || data.getDayOfWeek() > 7)) {
            return "星期几必须在1-7之间";
        }

        // 时间格式校验
        if (data.getStartTime() != null && !data.getStartTime().isEmpty() && !isValidTimeFormat(data.getStartTime())) {
            return "开始时间格式不正确，应为HH:mm:ss";
        }
        if (data.getEndTime() != null && !data.getEndTime().isEmpty() && !isValidTimeFormat(data.getEndTime())) {
            return "结束时间格式不正确，应为HH:mm:ss";
        }

        // 状态校验
        if (data.getStatus() != null && data.getStatus() < 0 && data.getStatus() > 2) {
            return "课程状态必须是0-未开课、1-已开课或2-已结课";
        }

        // 教材校验
        if (data.getTextbook() != null && data.getTextbook() != 0 && data.getTextbook() != 1) {
            return "教材必须是0-否或1-是";
        }

        // 外年级选课校验
        if (data.getExternalSelection() != null && data.getExternalSelection() != 0 && data.getExternalSelection() != 1) {
            return "外年级选课必须是0-否或1-是";
        }

        return null;
    }

    /**
     * 校验时间格式 HH:mm:ss
     */
    private boolean isValidTimeFormat(String time) {
        return time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$");
    }

    /**
     * 加上存储数据库
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveData() {
        if (cachedDataList.isEmpty()) {
            return;
        }

        log.info("开始存储{}条数据到数据库...", cachedDataList.size());

        for (CourseImportDTO dto : cachedDataList) {
            try {
                Course course = convertToCourse(dto);
                coursesToSave.add(course);
                successCount++;
            } catch (Exception e) {
                log.error("转换课程数据失败: {}", dto, e);
                failDetails.add(new CourseImportResultDTO.FailDetail(currentRow, dto.getCourseNo(), "数据转换失败: " + e.getMessage()));
            }
        }

        // 批量保存
        if (!coursesToSave.isEmpty()) {
            courseService.saveBatch(coursesToSave);
            coursesToSave.clear();
        }

        log.info("存储数据库成功！");
    }

    /**
     * 转换DTO为Course实体
     */
    private Course convertToCourse(CourseImportDTO dto) {
        Course course = new Course();
        course.setCourseNo(dto.getCourseNo());
        course.setName(dto.getName());
        course.setCredit(dto.getCredit());
        course.setHours(dto.getHours());
        course.setSemester(dto.getSemester());
        course.setYear(dto.getYear());

        // 设置默认值
        course.setMaxStudents(dto.getMaxStudents() != null ? dto.getMaxStudents() : 50);
        course.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        course.setTextbook(dto.getTextbook() != null ? dto.getTextbook() : 0);
        course.setExternalSelection(dto.getExternalSelection() != null ? dto.getExternalSelection() : 0);

        // 设置可选字段
        course.setDayOfWeek(dto.getDayOfWeek());
        course.setStartTime(dto.getStartTime());
        course.setEndTime(dto.getEndTime());
        course.setClassroom(dto.getClassroom());
        course.setMaxCredits(dto.getMaxCredits());
        course.setDescription(dto.getDescription());
        course.setStudyNature(dto.getStudyNature());
        course.setRemark(dto.getRemark());

        // 设置教师ID
        course.setTeacherId(getTeacherId(dto.getTeacherNo()));

        // 设置时间
        Date now = new Date();
        course.setCreateTime(now);
        course.setUpdateTime(now);

        return course;
    }

    /**
     * 根据教师工号获取教师ID，如果没有则使用待定教师
     */
    private Long getTeacherId(String teacherNo) {
        if (teacherNo == null || teacherNo.trim().isEmpty()) {
            return defaultTeacherId;
        }

        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
        wrapper.eq("teacher_no", teacherNo);
        Teacher teacher = teacherService.getOne(wrapper);
        if (teacher != null) {
            return teacher.getId();
        }

        return defaultTeacherId;
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
}
