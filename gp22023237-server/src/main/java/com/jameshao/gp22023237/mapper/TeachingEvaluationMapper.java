package com.jameshao.gp22023237.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jameshao.gp22023237.DTO.EvaluationCourseDTO;
import com.jameshao.gp22023237.DTO.TeacherEvaluationStatsDTO;
import com.jameshao.gp22023237.po.TeachingEvaluation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 教学评价Mapper
 */
public interface TeachingEvaluationMapper extends BaseMapper<TeachingEvaluation> {

    /**
     * 查询学生的待评价课程列表
     */
    List<EvaluationCourseDTO> listPendingCourses(@Param("studentId") Long studentId);

    /**
     * 查询学生的已评价课程列表
     */
    List<EvaluationCourseDTO> listCompletedCourses(@Param("studentId") Long studentId);

    /**
     * 检查某学生某课程是否已评价
     */
    boolean checkEvaluated(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 教师查看自己收到的评价统计
     */
    TeacherEvaluationStatsDTO getTeacherStats(@Param("teacherId") Long teacherId);
}
