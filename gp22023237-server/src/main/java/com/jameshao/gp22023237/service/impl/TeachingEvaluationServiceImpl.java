package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.DTO.EvaluationCourseDTO;
import com.jameshao.gp22023237.DTO.TeacherEvaluationStatsDTO;
import com.jameshao.gp22023237.DTO.TeachingEvaluationDTO;
import com.jameshao.gp22023237.mapper.TeachingEvaluationMapper;
import com.jameshao.gp22023237.mapper.CourseMapper;
import com.jameshao.gp22023237.mapper.ScheduleMapper;
import com.jameshao.gp22023237.po.Course;
import com.jameshao.gp22023237.po.Schedule;
import com.jameshao.gp22023237.po.TeachingEvaluation;
import com.jameshao.gp22023237.service.TeachingEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 教学评价服务实现类
 */
@Slf4j
@Service
public class TeachingEvaluationServiceImpl extends ServiceImpl<TeachingEvaluationMapper, TeachingEvaluation>
        implements TeachingEvaluationService {

    @Autowired
    private TeachingEvaluationMapper evaluationMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public boolean submitEvaluation(Long studentId, TeachingEvaluationDTO dto) {
        // 校验评分范围
        if (dto.getAttitudeScore() == null || dto.getAttitudeScore() < 1 || dto.getAttitudeScore() > 5
                || dto.getContentScore() == null || dto.getContentScore() < 1 || dto.getContentScore() > 5
                || dto.getMethodScore() == null || dto.getMethodScore() < 1 || dto.getMethodScore() > 5
                || dto.getEffectScore() == null || dto.getEffectScore() < 1 || dto.getEffectScore() > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }

        // 校验是否已评价
        if (checkEvaluated(studentId, dto.getCourseId())) {
            throw new IllegalStateException("您已评价过该课程，不可重复评价");
        }

        // 获取课程信息
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }

        // 从排课表获取教师ID（取第一条排课记录的教师）
        Long teacherId = null;
        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(Schedule::getCourseId, dto.getCourseId());
        scheduleWrapper.select(Schedule::getTeacherId);
        scheduleWrapper.last("LIMIT 1");
        Schedule schedule = scheduleMapper.selectOne(scheduleWrapper);
        if (schedule != null) {
            teacherId = schedule.getTeacherId();
        }

        TeachingEvaluation evaluation = new TeachingEvaluation();
        evaluation.setStudentId(studentId);
        evaluation.setCourseId(dto.getCourseId());
        evaluation.setTeacherId(teacherId);
        evaluation.setAttitudeScore(dto.getAttitudeScore());
        evaluation.setContentScore(dto.getContentScore());
        evaluation.setMethodScore(dto.getMethodScore());
        evaluation.setEffectScore(dto.getEffectScore());
        evaluation.setComment(dto.getComment());
        evaluation.setCreateTime(new Date());

        return save(evaluation);
    }

    @Override
    public List<EvaluationCourseDTO> listPendingCourses(Long studentId) {
        return evaluationMapper.listPendingCourses(studentId);
    }

    @Override
    public List<EvaluationCourseDTO> listCompletedCourses(Long studentId) {
        return evaluationMapper.listCompletedCourses(studentId);
    }

    @Override
    public boolean checkEvaluated(Long studentId, Long courseId) {
        return evaluationMapper.checkEvaluated(studentId, courseId);
    }

    @Override
    public TeacherEvaluationStatsDTO getTeacherStats(Long teacherId) {
        return evaluationMapper.getTeacherStats(teacherId);
    }
}
