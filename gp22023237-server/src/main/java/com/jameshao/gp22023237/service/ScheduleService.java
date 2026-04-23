package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.DTO.ScheduleDTO;
import com.jameshao.gp22023237.DTO.ScheduleImportResultDTO;
import com.jameshao.gp22023237.DTO.ScheduleWithDetailsDTO;
import com.jameshao.gp22023237.po.Schedule;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 排课Service接口
 */
public interface ScheduleService extends IService<Schedule> {

    /**
     * 查询排课列表（关联详情）
     */
    List<ScheduleWithDetailsDTO> listScheduleWithDetails(Long teacherId, Long classId, String semester, Integer year);

    /**
     * 查询排课列表（分页）
     */
    Map<String, Object> listScheduleWithDetailsPage(Long teacherId, Long classId, String semester, Integer year,
                                                     Integer pageNum, Integer pageSize);

    /**
     * 根据ID查询排课详情
     */
    ScheduleWithDetailsDTO getScheduleWithDetailsById(Long id);

    /**
     * 新增排课（支持多班级批量排课，含冲突校验）
     */
    String addSchedule(ScheduleDTO dto);

    /**
     * 更新排课（含冲突校验）
     */
    String updateSchedule(ScheduleDTO dto);

    /**
     * 删除排课
     */
    String deleteSchedule(Long id);

    /**
     * 复制排课记录
     */
    String copySchedule(Long id, Long newClassId, Integer newDayOfWeek, Long newStartSection, Long newEndSection);

    /**
     * 冲突校验（含教室冲突检测，返回冲突详情）
     */
    Map<String, Object> checkConflict(Long teacherId, Long classId, String classroom,
                                       Integer dayOfWeek, Long startSection, Long endSection,
                                       String semester, Integer year, Long excludeId);

    /**
     * 批量导入排课
     */
    ScheduleImportResultDTO importSchedules(MultipartFile file);
}
