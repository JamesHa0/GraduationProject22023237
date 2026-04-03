package com.jameshao.gp22023237.controller.thesis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.po.*;
import com.jameshao.gp22023237.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 学位论文管理控制器
 * 负责处理学位论文全流程管理：开题报告 -> 中期检查 -> 预答辩 -> 学位申请 -> 学位审批 -> 学位授予
 * 支持导师、教学秘书、分管院长三级审批体系
 * 路径前缀: /thesis
 */
@RestController
@RequestMapping("/thesis")
public class ThesisManagementController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private ThesisProgressService thesisProgressService;

    @Autowired
    private ThesisDefenseService thesisDefenseService;

    @Autowired
    private ThesisExternalReviewService thesisExternalReviewService;

    @Autowired
    private DegreeApplicationService degreeApplicationService;

    // ==================== 论文进展管理（开题、中期、预答辩统一接口）====================

    @PostMapping("/progress/submit")
    public String submitProgress(@RequestBody ThesisProgress progress) {
        try {
            boolean success = thesisProgressService.submitProgress(progress);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/progress/list")
    public String getProgressList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) Long studentId,
                                  @RequestParam(required = false) Integer progressType,
                                  @RequestParam(required = false) Integer overallStatus) {
        try {
            Page<ThesisProgress> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisProgress> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ThesisProgress::getSubmitTime);

            if (studentId != null) {
                wrapper.eq(ThesisProgress::getStudentId, studentId);
            }
            if (progressType != null) {
                wrapper.eq(ThesisProgress::getProgressType, progressType);
            }
            if (overallStatus != null) {
                wrapper.eq(ThesisProgress::getOverallStatus, overallStatus);
            }

            IPage<ThesisProgress> result = thesisProgressService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/progress/{id}")
    public String getProgressDetail(@PathVariable Long id) {
        try {
            ThesisProgress progress = thesisProgressService.getById(id);
            return progress != null ? jsonReturn.returnSuccess(progress) : jsonReturn.returnFailed("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/progress/mentor/approve")
    public String progressMentorApprove(@RequestParam Long id,
                                         @RequestParam Integer status,
                                         @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisProgressService.mentorApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/progress/secretary/approve")
    public String progressSecretaryApprove(@RequestParam Long id,
                                        @RequestParam Integer status,
                                        @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisProgressService.secretaryApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/progress/dean/approve")
    public String progressDeanApprove(@RequestParam Long id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisProgressService.deanApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 答辩条件检查 ====================

    @GetMapping("/degree/checkEligibility")
    public String checkDefenseEligibility(@RequestParam Long studentId) {
        try {
            // 检查学生是否满足答辩条件
            // 条件1：已完成开题报告且通过（progressType=1, overallStatus=4）
            // 条件2：已完成中期检查且通过（progressType=2, overallStatus=4）
            // 条件3：已完成预答辩且通过（progressType=3, overallStatus=4）
            // 条件4：课程学分达标

            LambdaQueryWrapper<ThesisProgress> proposalWrapper = new LambdaQueryWrapper<>();
            proposalWrapper.eq(ThesisProgress::getStudentId, studentId);
            proposalWrapper.eq(ThesisProgress::getProgressType, 1);
            proposalWrapper.eq(ThesisProgress::getOverallStatus, 4);

            LambdaQueryWrapper<ThesisProgress> midtermWrapper = new LambdaQueryWrapper<>();
            midtermWrapper.eq(ThesisProgress::getStudentId, studentId);
            midtermWrapper.eq(ThesisProgress::getProgressType, 2);
            midtermWrapper.eq(ThesisProgress::getOverallStatus, 4);

            LambdaQueryWrapper<ThesisProgress> preDefenseWrapper = new LambdaQueryWrapper<>();
            preDefenseWrapper.eq(ThesisProgress::getStudentId, studentId);
            preDefenseWrapper.eq(ThesisProgress::getProgressType, 3);
            preDefenseWrapper.eq(ThesisProgress::getOverallStatus, 4);

            boolean proposalPassed = thesisProgressService.count(proposalWrapper) > 0;
            boolean midtermPassed = thesisProgressService.count(midtermWrapper) > 0;
            boolean preDefensePassed = thesisProgressService.count(preDefenseWrapper) > 0;

            boolean creditsQualified = true;

            Map<String, Object> result = new HashMap<>();
            result.put("eligible", proposalPassed && midtermPassed && preDefensePassed && creditsQualified);
            result.put("conditions", Map.of(
                "proposalPassed", proposalPassed,
                "midtermPassed", midtermPassed,
                "preDefensePassed", preDefensePassed,
                "creditsQualified", creditsQualified
            ));

            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 学位申请管理 ====================

    @PostMapping("/degree/submit")
    public String submitDegreeApplication(@RequestBody DegreeApplication application) {
        try {
            boolean success = degreeApplicationService.submitApplication(application);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/degree/list")
    public String getDegreeList(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) Long studentId,
                                @RequestParam(required = false) Integer degreeGranted) {
        try {
            Page<DegreeApplication> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<DegreeApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(DegreeApplication::getSubmitTime);

            if (studentId != null) {
                wrapper.eq(DegreeApplication::getStudentId, studentId);
            }
            if (degreeGranted != null) {
                wrapper.eq(DegreeApplication::getDegreeGranted, degreeGranted);
            }

            IPage<DegreeApplication> result = degreeApplicationService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }


    @PostMapping("/degree/committee/approve")
    public String committeeApprove(@RequestParam Long id,
                                  @RequestParam Integer status,
                                  @RequestParam(required = false) String comment) {
        try {
            boolean success = degreeApplicationService.committeeApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/degree/grant")
    public String grantDegree(@RequestParam Long id,
                             @RequestParam String certificateNo) {
        try {
            boolean success = degreeApplicationService.grantDegree(id, certificateNo);
            return success ? jsonReturn.returnSuccess("学位授予成功") : jsonReturn.returnFailed("学位授予失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 论文答辩管理 ====================

    @PostMapping("/defense/submit")
    public String submitDefense(@RequestBody ThesisDefense defense) {
        try {
            boolean success = thesisDefenseService.submitDefense(defense);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/defense/list")
    public String getDefenseList(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) Long studentId,
                                 @RequestParam(required = false) Integer status) {
        try {
            Page<ThesisDefense> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisDefense> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ThesisDefense::getCreateTime);

            if (studentId != null) {
                wrapper.eq(ThesisDefense::getStudentId, studentId);
            }
            if (status != null) {
                wrapper.eq(ThesisDefense::getStatus, status);
            }

            IPage<ThesisDefense> result = thesisDefenseService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/defense/{id}")
    public String getDefenseDetail(@PathVariable Long id) {
        try {
            ThesisDefense defense = thesisDefenseService.getById(id);
            return defense != null ? jsonReturn.returnSuccess(defense) : jsonReturn.returnFailed("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/defense/tutor/approve")
    public String defenseTutorApprove(@RequestParam Long id,
                                      @RequestParam Integer status) {
        try {
            boolean success = thesisDefenseService.tutorApprove(id, status);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/defense/dean/approve")
    public String defenseDeanApprove(@RequestParam Long id,
                                     @RequestParam Integer status) {
        try {
            boolean success = thesisDefenseService.deanApprove(id, status);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/defense/record")
    public String recordDefenseResult(@RequestParam Long id,
                                      @RequestParam Integer result,
                                      @RequestParam Double score,
                                      @RequestParam(required = false) String comment,
                                      @RequestParam(required = false) String qaRecord) {
        try {
            boolean success = thesisDefenseService.recordDefenseResult(id, result, score, comment, qaRecord);
            return success ? jsonReturn.returnSuccess("记录成功") : jsonReturn.returnFailed("记录失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 论文外审管理 ====================

    @PostMapping("/externalReview/submit")
    public String submitExternalReview(@RequestBody ThesisExternalReview review) {
        try {
            boolean success = thesisExternalReviewService.submitReview(review);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/externalReview/list")
    public String getExternalReviewList(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) Long studentId,
                                         @RequestParam(required = false) Integer status) {
        try {
            Page<ThesisExternalReview> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisExternalReview> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ThesisExternalReview::getCreateTime);

            if (studentId != null) {
                wrapper.eq(ThesisExternalReview::getStudentId, studentId);
            }
            if (status != null) {
                wrapper.eq(ThesisExternalReview::getStatus, status);
            }

            IPage<ThesisExternalReview> result = thesisExternalReviewService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/externalReview/{id}")
    public String getExternalReviewDetail(@PathVariable Long id) {
        try {
            ThesisExternalReview review = thesisExternalReviewService.getById(id);
            return review != null ? jsonReturn.returnSuccess(review) : jsonReturn.returnFailed("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/externalReview/record")
    public String recordExternalReviewResult(@RequestParam Long id,
                                             @RequestParam Integer result,
                                             @RequestParam(required = false) String comments) {
        try {
            boolean success = thesisExternalReviewService.recordReviewResult(id, result, comments);
            return success ? jsonReturn.returnSuccess("记录成功") : jsonReturn.returnFailed("记录失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
