package com.jameshao.gp22023237.mapper;

import com.jameshao.gp22023237.DTO.ScheduleWithDetailsDTO;
import com.jameshao.gp22023237.po.Schedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 针对表【schedule(排课计划表)】的数据库操作Mapper
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    /**
     * 查询排课列表，关联教师/课程/班级/时间片信息
     */
    List<ScheduleWithDetailsDTO> listScheduleWithDetails(@Param("teacherId") Long teacherId,
                                                          @Param("classId") Long classId,
                                                          @Param("semester") String semester,
                                                          @Param("year") Integer year);

    /**
     * 查询排课列表，关联教师/课程/班级/时间片信息（分页）
     */
    List<ScheduleWithDetailsDTO> listScheduleWithDetailsPage(@Param("teacherId") Long teacherId,
                                                              @Param("classId") Long classId,
                                                              @Param("semester") String semester,
                                                              @Param("year") Integer year,
                                                              @Param("offset") Integer offset,
                                                              @Param("pageSize") Integer pageSize);

    /**
     * 统计排课记录总数
     */
    int countScheduleWithDetails(@Param("teacherId") Long teacherId,
                                 @Param("classId") Long classId,
                                 @Param("semester") String semester,
                                 @Param("year") Integer year);

    /**
     * 根据ID查询排课详情，关联教师/课程/班级/时间片信息
     */
    ScheduleWithDetailsDTO getScheduleWithDetailsById(@Param("id") Long id);

    /**
     * 检查教师在指定时间段是否已有排课（冲突校验，范围重叠判断）
     */
    int checkTeacherConflict(@Param("teacherId") Long teacherId,
                             @Param("dayOfWeek") Integer dayOfWeek,
                             @Param("startSection") Long startSection,
                             @Param("endSection") Long endSection,
                             @Param("semester") String semester,
                             @Param("year") Integer year,
                             @Param("excludeId") Long excludeId);

    /**
     * 检查班级在指定时间段是否已有排课（冲突校验，范围重叠判断）
     */
    int checkClassConflict(@Param("classId") Long classId,
                           @Param("dayOfWeek") Integer dayOfWeek,
                           @Param("startSection") Long startSection,
                           @Param("endSection") Long endSection,
                           @Param("semester") String semester,
                           @Param("year") Integer year,
                           @Param("excludeId") Long excludeId);

    /**
     * 检查教室在指定时间段是否已有排课（冲突校验，范围重叠判断）
     */
    int checkClassroomConflict(@Param("classroom") String classroom,
                               @Param("dayOfWeek") Integer dayOfWeek,
                               @Param("startSection") Long startSection,
                               @Param("endSection") Long endSection,
                               @Param("semester") String semester,
                               @Param("year") Integer year,
                               @Param("excludeId") Long excludeId);

    /**
     * 查询班级的学生人数
     */
    int countStudentsByClassId(@Param("classId") Long classId);
}
