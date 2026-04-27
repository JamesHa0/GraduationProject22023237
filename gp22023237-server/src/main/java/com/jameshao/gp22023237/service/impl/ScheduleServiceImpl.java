package com.jameshao.gp22023237.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.DTO.ScheduleDTO;
import com.jameshao.gp22023237.DTO.ScheduleImportDTO;
import com.jameshao.gp22023237.DTO.ScheduleImportResultDTO;
import com.jameshao.gp22023237.DTO.ScheduleWithDetailsDTO;
import com.jameshao.gp22023237.mapper.ClassMapper;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.mapper.ScheduleMapper;
import com.jameshao.gp22023237.po.Schedule;
import com.jameshao.gp22023237.service.ScheduleService;
import com.jameshao.gp22023237.service.DictDataService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.utils.ScheduleImportListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排课Service实现（含冲突校验）
 */
@Slf4j
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule>
    implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private DictDataService dictDataService;

    @Value("${schedule.import.batch-size:500}")
    private Integer importBatchSize;

    @Override
    public List<ScheduleWithDetailsDTO> listScheduleWithDetails(Long teacherId, Long classId, String semester, Integer year) {
        return scheduleMapper.listScheduleWithDetails(teacherId, classId, semester, year);
    }

    @Override
    public Map<String, Object> listScheduleWithDetailsPage(Long teacherId, Long classId, String semester, Integer year,
                                                            Integer pageNum, Integer pageSize) {
        Map<String, Object> data = new HashMap<>();
        if (pageNum != null && pageSize != null) {
            int offset = (pageNum - 1) * pageSize;
            List<ScheduleWithDetailsDTO> rows = scheduleMapper.listScheduleWithDetailsPage(teacherId, classId, semester, year, offset, pageSize);
            int total = scheduleMapper.countScheduleWithDetails(teacherId, classId, semester, year);
            data.put("rows", rows);
            data.put("total", total);
        } else {
            List<ScheduleWithDetailsDTO> list = scheduleMapper.listScheduleWithDetails(teacherId, classId, semester, year);
            data.put("rows", list);
            data.put("total", list.size());
        }
        return data;
    }

    @Override
    public ScheduleWithDetailsDTO getScheduleWithDetailsById(Long id) {
        return scheduleMapper.getScheduleWithDetailsById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addSchedule(ScheduleDTO dto) {
        // 冲突校验
        List<Long> classIds = dto.getClassIds();
        if (classIds == null || classIds.isEmpty()) {
            if (dto.getClassId() != null) {
                classIds = List.of(dto.getClassId());
            } else {
                return "请选择班级";
            }
        }

        List<String> conflictMessages = new ArrayList<>();
        List<Schedule> successList = new ArrayList<>();

        for (Long classId : classIds) {
            // 校验教师冲突
            int teacherConflict = scheduleMapper.checkTeacherConflict(
                dto.getTeacherId(), dto.getDayOfWeek(), dto.getStartSection(), dto.getEndSection(),
                dto.getSemester(), dto.getYear(), null);
            if (teacherConflict > 0) {
                conflictMessages.add("教师在" + getDayName(dto.getDayOfWeek()) + "该时间段已有排课");
                continue;
            }

            // 校验班级冲突
            int classConflict = scheduleMapper.checkClassConflict(
                classId, dto.getDayOfWeek(), dto.getStartSection(), dto.getEndSection(),
                dto.getSemester(), dto.getYear(), null);
            if (classConflict > 0) {
                conflictMessages.add("班级在" + getDayName(dto.getDayOfWeek()) + "该时间段已有排课");
                continue;
            }

            // 创建排课记录
            Schedule schedule = new Schedule();
            schedule.setTeacherId(dto.getTeacherId());
            schedule.setCourseId(dto.getCourseId());
            schedule.setClassId(classId);
            schedule.setStartSection(dto.getStartSection());
            schedule.setEndSection(dto.getEndSection());
            schedule.setDayOfWeek(dto.getDayOfWeek());
            schedule.setClassroom(dto.getClassroom());
            schedule.setSemester(dto.getSemester());
            schedule.setYear(dto.getYear());
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            successList.add(schedule);
        }

        if (!conflictMessages.isEmpty() && successList.isEmpty()) {
            return String.join("；", conflictMessages);
        }

        // 批量保存
        for (Schedule schedule : successList) {
            save(schedule);
        }

        if (!conflictMessages.isEmpty()) {
            return "部分排课成功，以下排课因冲突未添加：" + String.join("；", conflictMessages);
        }

        return null; // null表示全部成功
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateSchedule(ScheduleDTO dto) {
        Long excludeId = dto.getId();

        // 校验教师冲突
        int teacherConflict = scheduleMapper.checkTeacherConflict(
            dto.getTeacherId(), dto.getDayOfWeek(), dto.getStartSection(), dto.getEndSection(),
            dto.getSemester(), dto.getYear(), excludeId);
        if (teacherConflict > 0) {
            return "教师在" + getDayName(dto.getDayOfWeek()) + "该时间段已有排课";
        }

        // 校验班级冲突
        Long classId = dto.getClassId() != null ? dto.getClassId() : dto.getClassIds().get(0);
        int classConflict = scheduleMapper.checkClassConflict(
            classId, dto.getDayOfWeek(), dto.getStartSection(), dto.getEndSection(),
            dto.getSemester(), dto.getYear(), excludeId);
        if (classConflict > 0) {
            return "班级在" + getDayName(dto.getDayOfWeek()) + "该时间段已有排课";
        }

        // 更新排课记录
        Schedule schedule = getById(dto.getId());
        if (schedule == null) {
            return "排课记录不存在";
        }
        schedule.setTeacherId(dto.getTeacherId());
        schedule.setCourseId(dto.getCourseId());
        schedule.setClassId(classId);
        schedule.setStartSection(dto.getStartSection());
        schedule.setEndSection(dto.getEndSection());
        schedule.setDayOfWeek(dto.getDayOfWeek());
        schedule.setClassroom(dto.getClassroom());
        schedule.setSemester(dto.getSemester());
        schedule.setYear(dto.getYear());
        schedule.setUpdateTime(new Date());
        updateById(schedule);

        return null; // null表示成功
    }

    @Override
    public String deleteSchedule(Long id) {
        boolean success = removeById(id);
        return success ? null : "删除失败";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String copySchedule(Long id, Long newClassId, Integer newDayOfWeek, Long newStartSection, Long newEndSection) {
        Schedule original = getById(id);
        if (original == null) {
            return "原排课记录不存在";
        }

        // 校验教师冲突
        int teacherConflict = scheduleMapper.checkTeacherConflict(
            original.getTeacherId(), newDayOfWeek, newStartSection, newEndSection,
            original.getSemester(), original.getYear(), null);
        if (teacherConflict > 0) {
            return "教师在" + getDayName(newDayOfWeek) + "该时间段已有排课";
        }

        // 校验班级冲突
        int classConflict = scheduleMapper.checkClassConflict(
            newClassId, newDayOfWeek, newStartSection, newEndSection,
            original.getSemester(), original.getYear(), null);
        if (classConflict > 0) {
            return "班级在" + getDayName(newDayOfWeek) + "该时间段已有排课";
        }

        // 创建新排课记录
        Schedule newSchedule = new Schedule();
        newSchedule.setTeacherId(original.getTeacherId());
        newSchedule.setCourseId(original.getCourseId());
        newSchedule.setClassId(newClassId);
        newSchedule.setStartSection(newStartSection);
        newSchedule.setEndSection(newEndSection);
        newSchedule.setDayOfWeek(newDayOfWeek);
        newSchedule.setClassroom(original.getClassroom());
        newSchedule.setSemester(original.getSemester());
        newSchedule.setYear(original.getYear());
        newSchedule.setCreateTime(new Date());
        newSchedule.setUpdateTime(new Date());
        save(newSchedule);

        return null;
    }

    @Override
    public Map<String, Object> checkConflict(Long teacherId, Long classId, String classroom,
                                               Integer dayOfWeek, Long startSection, Long endSection,
                                               String semester, Integer year, Long excludeId) {
        Map<String, Object> result = new HashMap<>();
        List<String> conflicts = new ArrayList<>();
        List<Map<String, String>> conflictDetails = new ArrayList<>();

        if (teacherId != null) {
            int teacherConflict = scheduleMapper.checkTeacherConflict(teacherId, dayOfWeek, startSection, endSection, semester, year, excludeId);
            if (teacherConflict > 0) {
                conflicts.add("teacher");
                Map<String, String> detail = new HashMap<>();
                detail.put("type", "teacher");
                detail.put("message", "教师在" + getDayName(dayOfWeek) + "该时间段已有排课");
                conflictDetails.add(detail);
            }
        }

        if (classId != null) {
            int classConflict = scheduleMapper.checkClassConflict(classId, dayOfWeek, startSection, endSection, semester, year, excludeId);
            if (classConflict > 0) {
                conflicts.add("class");
                Map<String, String> detail = new HashMap<>();
                detail.put("type", "class");
                detail.put("message", "班级在" + getDayName(dayOfWeek) + "该时间段已有排课");
                conflictDetails.add(detail);
            }
        }

        if (classroom != null && !classroom.trim().isEmpty()) {
            int classroomConflict = scheduleMapper.checkClassroomConflict(classroom.trim(), dayOfWeek, startSection, endSection, semester, year, excludeId);
            if (classroomConflict > 0) {
                conflicts.add("classroom");
                Map<String, String> detail = new HashMap<>();
                detail.put("type", "classroom");
                detail.put("message", "教室" + classroom.trim() + "在" + getDayName(dayOfWeek) + "该时间段已有排课");
                conflictDetails.add(detail);
            }
        }

        result.put("hasConflict", !conflicts.isEmpty());
        result.put("conflicts", conflicts);
        result.put("conflictDetails", conflictDetails);
        return result;
    }

    private String getDayName(Integer dayOfWeek) {
        if (dayOfWeek == null) return "";
        String[] days = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : "";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScheduleImportResultDTO importSchedules(MultipartFile file) {
        long start = System.currentTimeMillis();
        try {
            int batchSize = (importBatchSize == null || importBatchSize <= 0) ? 500 : importBatchSize;

            ScheduleImportListener listener = new ScheduleImportListener(
                    this, teacherService, courseMapper, classMapper, scheduleMapper, dictDataService, batchSize);
            EasyExcel.read(file.getInputStream(), ScheduleImportDTO.class, listener).sheet().doRead();
            ScheduleImportResultDTO result = listener.getResult();
            long cost = System.currentTimeMillis() - start;
            log.info("排课导入完成，总记录:{}，成功:{}，失败:{}，总耗时:{}ms，批次:{}",
                    result.getTotal(), result.getSuccessCount(), result.getFailCount(), cost, batchSize);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("解析Excel文件失败，请检查文件格式", e);
        }
    }
}
