package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.DTO.EvaluationCourseDTO;
import com.jameshao.gp22023237.DTO.TeacherEvaluationStatsDTO;
import com.jameshao.gp22023237.DTO.TeachingEvaluationDTO;

import java.util.List;

/**
 * 教学评价服务接口
 */
public interface TeachingEvaluationService {

    /**
     * 学生提交教学评价
     * @param studentId 学生ID
     * @param dto 评价DTO
     * @return 是否成功
     */
    boolean submitEvaluation(Long studentId, TeachingEvaluationDTO dto);

    /**
     * 查询学生的待评价课程列表
     */
    List<EvaluationCourseDTO> listPendingCourses(Long studentId);

    /**
     * 查询学生的已评价课程列表
     */
    List<EvaluationCourseDTO> listCompletedCourses(Long studentId);

    /**
     * 检查某学生某课程是否已评价
     */
    boolean checkEvaluated(Long studentId, Long courseId);

    /**
     * 教师查看自己收到的评价统计
     */
    TeacherEvaluationStatsDTO getTeacherStats(Long teacherId);
}
