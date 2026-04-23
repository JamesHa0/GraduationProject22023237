package com.jameshao.gp22023237.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jameshao.gp22023237.DTO.ScheduleImportDTO;
import com.jameshao.gp22023237.DTO.ScheduleImportResultDTO;
import com.jameshao.gp22023237.common.error.ScheduleImportErrorCode;
import com.jameshao.gp22023237.mapper.ClassMapper;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.mapper.ScheduleMapper;
import com.jameshao.gp22023237.po.ClassEntity;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.po.Schedule;
import com.jameshao.gp22023237.po.DictData;
import com.jameshao.gp22023237.po.Teacher;
import com.jameshao.gp22023237.service.ScheduleService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.service.DictDataService;
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
 * 排课导入Excel监听器
 */
@Slf4j
public class ScheduleImportListener implements ReadListener<ScheduleImportDTO> {

    private final int batchCount;
    private List<RowData> cachedDataList;

    private final ScheduleService scheduleService;
    private final TeacherService teacherService;
    private final CourseMapper courseMapper;
    private final ClassMapper classMapper;
    private final ScheduleMapper scheduleMapper;
    private final DictDataService dictDataService;

    private final List<ScheduleImportResultDTO.FailDetail> failDetails = new ArrayList<>();
    private int successCount = 0;
    private int totalCount = 0;

    /** 文件内判重：teacherNo_courseNo_className_dayOfWeek_startSection_endSection_semester_year */
    private final Set<String> fileRowSet = new HashSet<>();

    /** 预加载缓存 */
    private Map<String, Teacher> teacherNoMap;
    private Map<String, Course> courseNoMap;
    private Map<String, ClassEntity> classNameMap;
    private Map<String, DictData> sectionNameMap; // dictLabel -> DictData

    public ScheduleImportListener(ScheduleService scheduleService,
                                  TeacherService teacherService,
                                  CourseMapper courseMapper,
                                  ClassMapper classMapper,
                                  ScheduleMapper scheduleMapper,
                                  DictDataService dictDataService,
                                  int batchCount) {
        this.scheduleService = scheduleService;
        this.teacherService = teacherService;
        this.courseMapper = courseMapper;
        this.classMapper = classMapper;
        this.scheduleMapper = scheduleMapper;
        this.dictDataService = dictDataService;
        this.batchCount = batchCount;
        this.cachedDataList = ListUtils.newArrayListWithExpectedSize(batchCount);
    }

    @Override
    public void invoke(ScheduleImportDTO data, AnalysisContext context) {
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
        log.info("排课导入解析完成，总记录:{}，成功:{}，失败:{}", totalCount, successCount, failDetails.size());
    }

    /**
     * 预加载基础数据（仅首次调用时加载）
     */
    private void ensurePreload() {
        if (teacherNoMap != null) return;

        // 教师缓存：teacherNo -> Teacher
        teacherNoMap = new HashMap<>();
        List<Teacher> teachers = teacherService.list();
        for (Teacher t : teachers) {
            if (t.getTeacherNo() != null) {
                teacherNoMap.put(t.getTeacherNo().trim(), t);
            }
        }

        // 课程缓存：courseNo -> Course
        courseNoMap = new HashMap<>();
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.select(Course::getId, Course::getCourseNo, Course::getName);
        List<Course> courses = courseMapper.selectList(courseWrapper);
        for (Course c : courses) {
            if (c.getCourseNo() != null) {
                courseNoMap.put(c.getCourseNo().trim(), c);
            }
        }

        // 班级缓存：className -> ClassEntity
        classNameMap = new HashMap<>();
        List<ClassEntity> classes = classMapper.selectList(null);
        for (ClassEntity cl : classes) {
            if (cl.getClassName() != null) {
                classNameMap.put(cl.getClassName().trim(), cl);
            }
        }

        // 节次缓存：dictLabel -> DictData（单节时间片）
        sectionNameMap = new HashMap<>();
        LambdaQueryWrapper<DictData> slotWrapper = new LambdaQueryWrapper<>();
        slotWrapper.eq(DictData::getDictType, "sys_time_slot");
        slotWrapper.eq(DictData::getStatus, "0");
        List<DictData> slots = dictDataService.list(slotWrapper);
        for (DictData dd : slots) {
            if (dd.getDictLabel() != null) {
                sectionNameMap.put(dd.getDictLabel().trim(), dd);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveData() {
        if (cachedDataList.isEmpty()) return;
        ensurePreload();

        long start = System.currentTimeMillis();
        List<Schedule> validSchedules = new ArrayList<>();

        for (RowData rowData : cachedDataList) {
            ScheduleImportResultDTO.FailDetail failDetail = validateData(rowData);
            if (failDetail != null) {
                failDetails.add(failDetail);
                continue;
            }
            try {
                Schedule schedule = convertToSchedule(rowData.dto);
                if (schedule == null) {
                failDetails.add(toFailDetail(rowData.row, rowData.dto,
                        ScheduleImportErrorCode.SYSTEM_ERROR, "数据转换失败"));
                    continue;
                }

                // 冲突校验（范围重叠）
                int teacherConflict = scheduleMapper.checkTeacherConflict(
                        schedule.getTeacherId(), schedule.getDayOfWeek(),
                        schedule.getStartSection(), schedule.getEndSection(),
                        schedule.getSemester(), schedule.getYear(), null);
                if (teacherConflict > 0) {
                    failDetails.add(toFailDetail(rowData.row, rowData.dto,
                            ScheduleImportErrorCode.TEACHER_CONFLICT));
                    continue;
                }

                int classConflict = scheduleMapper.checkClassConflict(
                        schedule.getClassId(), schedule.getDayOfWeek(),
                        schedule.getStartSection(), schedule.getEndSection(),
                        schedule.getSemester(), schedule.getYear(), null);
                if (classConflict > 0) {
                    failDetails.add(toFailDetail(rowData.row, rowData.dto,
                            ScheduleImportErrorCode.CLASS_CONFLICT));
                    continue;
                }

                // 教室冲突校验
                if (schedule.getClassroom() != null && !schedule.getClassroom().trim().isEmpty()) {
                    int classroomConflict = scheduleMapper.checkClassroomConflict(
                            schedule.getClassroom().trim(), schedule.getDayOfWeek(),
                            schedule.getStartSection(), schedule.getEndSection(),
                            schedule.getSemester(), schedule.getYear(), null);
                    if (classroomConflict > 0) {
                        failDetails.add(toFailDetail(rowData.row, rowData.dto,
                                ScheduleImportErrorCode.CLASSROOM_CONFLICT));
                        continue;
                    }
                }

                validSchedules.add(schedule);

                // 加入文件判重集合
                String rowKey = buildRowKey(rowData.dto);
                fileRowSet.add(rowKey);
            } catch (Exception e) {
                log.error("第{}行数据转换失败", rowData.row, e);
                failDetails.add(toFailDetail(rowData.row, rowData.dto,
                        ScheduleImportErrorCode.SYSTEM_ERROR, "数据转换失败: " + e.getMessage()));
            }
        }

        if (!validSchedules.isEmpty()) {
            scheduleService.saveBatch(validSchedules, batchCount);
            successCount += validSchedules.size();
        }

        long cost = System.currentTimeMillis() - start;
        log.info("排课导入批次处理完成，批次大小:{}，成功:{}，失败:{}，耗时:{}ms",
                cachedDataList.size(), validSchedules.size(), cachedDataList.size() - validSchedules.size(), cost);
    }

    private String buildRowKey(ScheduleImportDTO dto) {
        return (dto.getTeacherNo() == null ? "" : dto.getTeacherNo().trim()) + "_" +
               (dto.getCourseNo() == null ? "" : dto.getCourseNo().trim()) + "_" +
               (dto.getClassName() == null ? "" : dto.getClassName().trim()) + "_" +
               (dto.getDayOfWeek() == null ? "" : dto.getDayOfWeek().trim()) + "_" +
               (dto.getStartSection() == null ? "" : dto.getStartSection().trim()) + "_" +
               (dto.getEndSection() == null ? "" : dto.getEndSection().trim()) + "_" +
               (dto.getSemester() == null ? "" : dto.getSemester().trim()) + "_" +
               (dto.getYear() == null ? "" : dto.getYear().trim());
    }

    private ScheduleImportResultDTO.FailDetail validateData(RowData rowData) {
        ScheduleImportDTO data = rowData.dto;

        // 教师工号必填
        if (data.getTeacherNo() == null || data.getTeacherNo().trim().isEmpty()) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.TEACHER_NO_REQUIRED);
        }
        String teacherNo = data.getTeacherNo().trim();

        // 教师工号存在性
        Teacher teacher = teacherNoMap.get(teacherNo);
        if (teacher == null) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.TEACHER_NOT_FOUND);
        }

        // 课程编号必填
        if (data.getCourseNo() == null || data.getCourseNo().trim().isEmpty()) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.COURSE_NO_REQUIRED);
        }
        String courseNo = data.getCourseNo().trim();

        // 课程编号存在性
        Course course = courseNoMap.get(courseNo);
        if (course == null) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.COURSE_NOT_FOUND);
        }

        // 班级名称必填
        if (data.getClassName() == null || data.getClassName().trim().isEmpty()) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.CLASS_NAME_REQUIRED);
        }
        String className = data.getClassName().trim();

        // 班级名称存在性
        ClassEntity classEntity = classNameMap.get(className);
        if (classEntity == null) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.CLASS_NOT_FOUND);
        }

        // 星期必填
        if (data.getDayOfWeek() == null || data.getDayOfWeek().trim().isEmpty()) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.DAY_OF_WEEK_REQUIRED);
        }

        // 星期校验
        Integer dayOfWeek = parseDayOfWeek(data.getDayOfWeek().trim());
        if (dayOfWeek == null) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.DAY_OF_WEEK_INVALID);
        }

        // 开始节次必填
        if (data.getStartSection() == null || data.getStartSection().trim().isEmpty()) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.TIME_SLOT_REQUIRED);
        }

        // 结束节次必填
        if (data.getEndSection() == null || data.getEndSection().trim().isEmpty()) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.TIME_SLOT_REQUIRED);
        }

        // 开始节次存在性（支持纯数字和"第x节"格式）
        DictData startSlot = resolveSection(data.getStartSection().trim());
        if (startSlot == null) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.TIME_SLOT_NOT_FOUND);
        }

        // 结束节次存在性（支持纯数字和"第x节"格式）
        DictData endSlot = resolveSection(data.getEndSection().trim());
        if (endSlot == null) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.TIME_SLOT_NOT_FOUND);
        }

        // 结束节次不能早于开始节次
        if (endSlot.getDictCode() < startSlot.getDictCode()) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.END_BEFORE_START);
        }

        // 学期必填
        if (data.getSemester() == null || data.getSemester().trim().isEmpty()) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.SEMESTER_REQUIRED);
        }

        // 学年必填
        if (data.getYear() == null || data.getYear().trim().isEmpty()) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.YEAR_REQUIRED);
        }

        // 学年范围
        try {
            int yearVal = Integer.parseInt(data.getYear().trim());
            if (yearVal < 2000 || yearVal > 2100) {
                return toFailDetail(rowData.row, data, ScheduleImportErrorCode.YEAR_INVALID);
            }
        } catch (NumberFormatException e) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.YEAR_INVALID);
        }

        // 文件内重复行检查
        String rowKey = buildRowKey(data);
        if (fileRowSet.contains(rowKey)) {
            return toFailDetail(rowData.row, data, ScheduleImportErrorCode.FILE_ROW_DUPLICATE);
        }

        // 暂不加入fileRowSet，等saveData成功后加入
        return null;
    }

    /**
     * 解析节次输入，支持纯数字和"第x节"格式
     * 纯数字如 "1" 会自动转换为 "第1节" 去匹配
     */
    private DictData resolveSection(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        // 先直接用原始输入匹配
        DictData slot = sectionNameMap.get(trimmed);
        if (slot != null) return slot;
        // 纯数字：补"第x节"再试
        try {
            int num = Integer.parseInt(trimmed);
            String label = "第" + num + "节";
            return sectionNameMap.get(label);
        } catch (NumberFormatException ignored) {}
        return null;
    }

    /**
     * 解析星期值，支持数字和中文
     */
    private Integer parseDayOfWeek(String val) {
        // 数字
        try {
            int v = Integer.parseInt(val);
            if (v >= 1 && v <= 7) return v;
        } catch (NumberFormatException ignored) {}

        // 中文
        Map<String, Integer> dayMap = new HashMap<>();
        dayMap.put("周一", 1); dayMap.put("星期一", 1); dayMap.put("一", 1);
        dayMap.put("周二", 2); dayMap.put("星期二", 2); dayMap.put("二", 2);
        dayMap.put("周三", 3); dayMap.put("星期三", 3); dayMap.put("三", 3);
        dayMap.put("周四", 4); dayMap.put("星期四", 4); dayMap.put("四", 4);
        dayMap.put("周五", 5); dayMap.put("星期五", 5); dayMap.put("五", 5);
        dayMap.put("周六", 6); dayMap.put("星期六", 6); dayMap.put("六", 6);
        dayMap.put("周日", 7); dayMap.put("星期日", 7); dayMap.put("七", 7); dayMap.put("天", 7);

        return dayMap.get(val);
    }

    private Schedule convertToSchedule(ScheduleImportDTO dto) {
        Teacher teacher = teacherNoMap.get(dto.getTeacherNo().trim());
        Course course = courseNoMap.get(dto.getCourseNo().trim());
        ClassEntity classEntity = classNameMap.get(dto.getClassName().trim());
        DictData startSlot = resolveSection(dto.getStartSection().trim());
        DictData endSlot = resolveSection(dto.getEndSection().trim());

        if (teacher == null || course == null || classEntity == null || startSlot == null || endSlot == null) {
            return null;
        }

        Schedule schedule = new Schedule();
        schedule.setTeacherId(teacher.getId());
        schedule.setCourseId(course.getId());
        schedule.setClassId(classEntity.getId());
        schedule.setStartSection(startSlot.getDictCode());
        schedule.setEndSection(endSlot.getDictCode());
        schedule.setDayOfWeek(parseDayOfWeek(dto.getDayOfWeek().trim()));
        schedule.setClassroom(dto.getClassroom() != null ? dto.getClassroom().trim() : null);
        schedule.setSemester(dto.getSemester().trim());
        schedule.setYear(Integer.parseInt(dto.getYear().trim()));
        schedule.setCreateTime(new Date());
        schedule.setUpdateTime(new Date());

        return schedule;
    }

    private ScheduleImportResultDTO.FailDetail toFailDetail(int row, ScheduleImportDTO dto, ScheduleImportErrorCode code) {
        return new ScheduleImportResultDTO.FailDetail(
                row,
                dto.getTeacherNo(),
                dto.getCourseNo(),
                dto.getCourseName(),
                dto.getClassName(),
                code.getMessage(), code.getCode(), code.getField(), code.getSuggestion(), code.getRule());
    }

    private ScheduleImportResultDTO.FailDetail toFailDetail(int row, ScheduleImportDTO dto, ScheduleImportErrorCode code, String customReason) {
        return new ScheduleImportResultDTO.FailDetail(
                row,
                dto.getTeacherNo(),
                dto.getCourseNo(),
                dto.getCourseName(),
                dto.getClassName(),
                customReason, code.getCode(), code.getField(), code.getSuggestion(), code.getRule());
    }

    public ScheduleImportResultDTO getResult() {
        return new ScheduleImportResultDTO(totalCount, successCount, failDetails.size(), failDetails);
    }

    private static class RowData {
        private final int row;
        private final ScheduleImportDTO dto;

        private RowData(int row, ScheduleImportDTO dto) {
            this.row = row;
            this.dto = dto;
        }
    }
}
