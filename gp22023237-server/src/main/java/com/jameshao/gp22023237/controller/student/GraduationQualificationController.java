package com.jameshao.gp22023237.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.GraduationQualification;
import com.jameshao.gp22023237.service.GraduationQualificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 毕业资格审核Controller
 */
@RestController
@RequestMapping("/student/graduation")
public class GraduationQualificationController {

    @Autowired
    private GraduationQualificationService graduationQualificationService;

    @Autowired
    private JSONReturn jsonReturn;

    /**
     * 自动审核毕业资格
     */
    @PostMapping("/autoReview")
    public JSONReturn autoReview(@RequestParam Long studentId, @RequestParam Long reviewerId) {
        try {
            boolean success = graduationQualificationService.autoReviewGraduation(studentId, reviewerId);
            if (success) {
                // 查询审核结果
                GraduationQualification qualification = graduationQualificationService.getOne(
                        new QueryWrapper<GraduationQualification>().eq("student_id", studentId));
                return jsonReturn.success("自动审核成功", qualification);
            } else {
                return jsonReturn.fail("自动审核失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail("自动审核失败：" + e.getMessage());
        }
    }

    /**
     * 手动审核毕业资格
     */
    @PostMapping("/manualReview")
    public JSONReturn manualReview(@RequestParam Long id,
                                     @RequestParam Integer creditCheck,
                                     @RequestParam Integer thesisCheck,
                                     @RequestParam Integer practiceCheck,
                                     @RequestParam(required = false) String comment,
                                     @RequestParam Long reviewerId) {
        try {
            boolean success = graduationQualificationService.manualReview(
                    id, creditCheck, thesisCheck, practiceCheck, comment, reviewerId);
            if (success) {
                return jsonReturn.success("手动审核成功");
            } else {
                return jsonReturn.fail("手动审核失败");
            }
        } catch (Exception e) {
            return jsonReturn.fail("手动审核失败：" + e.getMessage());
        }
    }

    /**
     * 查询毕业资格列表（分页）
     */
    @GetMapping("/list")
    public JSONReturn list(@RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(required = false) Long studentId,
                           @RequestParam(required = false) Integer overallResult) {
        try {
            QueryWrapper<GraduationQualification> queryWrapper = new QueryWrapper<>();
            if (studentId != null) {
                queryWrapper.eq("student_id", studentId);
            }
            if (overallResult != null) {
                queryWrapper.eq("overall_result", overallResult);
            }
            queryWrapper.orderByDesc("create_time");

            Page<GraduationQualification> page = graduationQualificationService.page(
                    new Page<>(pageNum, pageSize), queryWrapper);

            return jsonReturn.success("查询查询成功", page);
        } catch (Exception e) {
            return jsonReturn.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public JSONReturn getById(@PathVariable Long id) {
        try {
            GraduationQualification qualification = graduationQualificationService.getById(id);
            if (qualification != null) {
                return jsonReturn.success("查询成功", qualification);
            } else {
                return jsonReturn.fail("未找到审核记录");
            }
        } catch (Exception e) {
            return jsonReturn.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生毕业资格
     */
    @GetMapping("/student/{studentId}")
    public JSONReturn getByStudentId(@PathVariable Long studentId) {
        try {
            GraduationQualification qualification = graduationQualificationService.getOne(
                    new QueryWrapper<GraduationQualification>().eq("student_id", studentId));
            if (qualification != null) {
                return jsonReturn.success("查询成功", qualification);
            } else {
                return jsonReturn.success("暂无审核记录", null);
            }
        } catch (Exception e) {
            return jsonReturn.fail("查询失败：" + e.getMessage());
        }
    }
}
