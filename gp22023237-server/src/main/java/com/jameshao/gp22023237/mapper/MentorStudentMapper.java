package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.po.MentorStudent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author test
* @description 针对表【mentor_student(双选关系表)】的数据库操作Mapper
* @createDate 2025-10-08 18:16:25
* @Entity com.jameshao.gp22023237.po.MentorStudent
*/
public interface MentorStudentMapper extends BaseMapper<MentorStudent> {

    /**
     * 分页查询导师学生关系（关联学生和导师信息）
     */
    IPage<Map<String, Object>> pageRelationship(Page<Map<String, Object>> page,
                                                   @Param("studentId") Long studentId,
                                                   @Param("mentorId") Long mentorId,
                                                   @Param("onlyUndetermined") Boolean onlyUndetermined);

    /**
     * 获取可选学生列表（还没有确定导师的学生）
     */
    List<Map<String, Object>> listAvailableStudents();

    /**
     * 获取可选导师列表（还有剩余名额的导师）
     */
    List<Map<String, Object>> listAvailableMentors();

    /**
     * 获取学生当前的导师信息
     */
    Map<String, Object> getStudentCurrentMentor(@Param("studentId") Long studentId);

    /**
     * 获取学生的所有志愿（用于导出）
     */
    List<Map<String, Object>> getStudentVolunteers(@Param("studentId") Long studentId);

    /**
     * 获取学生基本信息（用于导出）
     */
    Map<String, Object> getStudentInfo(@Param("studentId") Long studentId);

    /**
     * 获取已确认的导师学生关系列表（用于导出汇总表）
     */
    List<Map<String, Object>> listConfirmedRelationships();
}




