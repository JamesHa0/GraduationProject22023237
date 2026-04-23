package com.jameshao.gp22023237.controller.course;

import com.jameshao.gp22023237.DTO.EvaluationCourseDTO;
import com.jameshao.gp22023237.DTO.TeacherEvaluationStatsDTO;
import com.jameshao.gp22023237.DTO.TeachingEvaluationDTO;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.service.CoursePhaseService;
import com.jameshao.gp22023237.service.TeachingEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教学评价Controller
 */
@Slf4j
@RestController
@RequestMapping("/course/evaluation")
public class TeachingEvaluationController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private TeachingEvaluationService evaluationService;

    @Autowired
    private CoursePhaseService coursePhaseService;

    /**
     * 提交教学评价
     */
    @PostMapping("/submit")
    public String submit(@RequestBody TeachingEvaluationDTO dto, @RequestParam Long studentId) {
        try {
            // 校验是否在成绩录入阶段
            if (!coursePhaseService.isScoreEntryOpen()) {
                return jsonReturn.returnError("当前不在成绩录入阶段，无法提交教学评价");
            }
            if (studentId == null) {
                return jsonReturn.returnError("学生ID不能为空");
            }
            if (dto.getCourseId() == null) {
                return jsonReturn.returnError("课程ID不能为空");
            }
            boolean success = evaluationService.submitEvaluation(studentId, dto);
            if (success) {
                return jsonReturn.returnSuccess("评价提交成功");
            } else {
                return jsonReturn.returnFailed("评价提交失败");
            }
        } catch (Exception e) {
            log.error("提交教学评价失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询待评价课程列表
     */
    @GetMapping("/pending")
    public String pendingCourses(@RequestParam Long studentId) {
        try {
            if (studentId == null) {
                return jsonReturn.returnError("学生ID不能为空");
            }
            List<EvaluationCourseDTO> list = evaluationService.listPendingCourses(studentId);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            log.error("查询待评价课程失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 查询已评价课程列表
     */
    @GetMapping("/completed")
    public String completedCourses(@RequestParam Long studentId) {
        try {
            if (studentId == null) {
                return jsonReturn.returnError("学生ID不能为空");
            }
            List<EvaluationCourseDTO> list = evaluationService.listCompletedCourses(studentId);
            return jsonReturn.returnSuccess(list);
        } catch (Exception e) {
            log.error("查询已评价课程失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 检查某学生某课程是否已评价
     */
    @GetMapping("/check")
    public String checkEvaluated(@RequestParam Long studentId, @RequestParam Long courseId) {
        try {
            boolean evaluated = evaluationService.checkEvaluated(studentId, courseId);
            Map<String, Object> data = new HashMap<>();
            data.put("evaluated", evaluated);
            return jsonReturn.returnSuccess(data);
        } catch (Exception e) {
            log.error("检查评价状态失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 教师查看自己收到的评价统计
     */
    @GetMapping("/teacherStats")
    public String teacherStats(@RequestParam Long teacherId) {
        try {
            if (teacherId == null) {
                return jsonReturn.returnError("教师ID不能为空");
            }
            TeacherEvaluationStatsDTO stats = evaluationService.getTeacherStats(teacherId);
            return jsonReturn.returnSuccess(stats);
        } catch (Exception e) {
            log.error("查询教师评价统计失败", e);
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
