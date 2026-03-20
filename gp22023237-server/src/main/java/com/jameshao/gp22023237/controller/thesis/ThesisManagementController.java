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

@RestController
@RequestMapping("/thesis")
public class ThesisManagementController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private ThesisProposalService thesisProposalService;

    @Autowired
    private ThesisMidtermService thesisMidtermService;

    @Autowired
    private ThesisPreDefenseService thesisPreDefenseService;

    @Autowired
    private DegreeApplicationService degreeApplicationService;

    // ==================== 开题报告管理 ====================

    @PostMapping("/proposal/submit")
    public String submitProposal(@RequestBody ThesisProposal proposal) {
        try {
            boolean success = thesisProposalService.submitProposal(proposal);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/proposal/list")
    public String getProposalList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) Long studentId,
                                  @RequestParam(required = false) Integer overallStatus) {
        try {
            Page<ThesisProposal> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisProposal> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ThesisProposal::getSubmitTime);

            if (studentId != null) {
                wrapper.eq(ThesisProposal::getStudentId, studentId);
            }
            if (overallStatus != null) {
                wrapper.eq(ThesisProposal::getOverallStatus, overallStatus);
            }

            IPage<ThesisProposal> result = thesisProposalService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/proposal/{id}")
    public String getProposalDetail(@PathVariable Long id) {
        try {
            ThesisProposal proposal = thesisProposalService.getById(id);
            return proposal != null ? jsonReturn.returnSuccess(proposal) : jsonReturn.returnFailed("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/proposal/mentor/approve")
    public String proposalMentorApprove(@RequestParam Long id,
                                         @RequestParam Integer status,
                                         @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisProposalService.mentorApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/proposal/secretary/approve")
    public String proposalSecretaryApprove(@RequestParam Long id,
                                        @RequestParam Integer status,
                                        @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisProposalService.secretaryApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/proposal/dean/approve")
    public String proposalDeanApprove(@RequestParam Long id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisProposalService.deanApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 中期检查管理 ====================

    @PostMapping("/midterm/submit")
    public String submitMidterm(@RequestBody ThesisMidterm midterm) {
        try {
            boolean success = thesisMidtermService.submitMidterm(midterm);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/midterm/list")
    public String getMidtermList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) Long studentId,
                                  @RequestParam(required = false) Integer overallStatus) {
        try {
            Page<ThesisMidterm> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisMidterm> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ThesisMidterm::getSubmitTime);

            if (studentId != null) {
                wrapper.eq(ThesisMidterm::getStudentId, studentId);
            }
            if (overallStatus != null) {
                wrapper.eq(ThesisMidterm::getOverallStatus, overallStatus);
            }

            IPage<ThesisMidterm> result = thesisMidtermService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/midterm/mentor/approve")
    public String midtermMentorApprove(@RequestParam Long id,
                                      @RequestParam Integer status,
                                      @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisMidtermService.mentorApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/midterm/secretary/approve")
    public String midtermSecretaryApprove(@RequestParam Long id,
                                         @RequestParam Integer status,
                                         @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisMidtermService.secretaryApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/midterm/dean/approve")
    public String midtermDeanApprove(@RequestParam Long id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisMidtermService.deanApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 预答辩管理 ====================

    @PostMapping("/preDefense/submit")
    public String submitPreDefense(@RequestBody ThesisPreDefense preDefense) {
        try {
            boolean success = thesisPreDefenseService.submitPreDefense(preDefense);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnFailed("提交失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/preDefense/list")
    public String getPreDefenseList(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) Long studentId,
                                     @RequestParam(required = false) Integer overallStatus) {
        try {
            Page<ThesisPreDefense> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisPreDefense> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ThesisPreDefense::getSubmitTime);

            if (studentId != null) {
                wrapper.eq(ThesisPreDefense::getStudentId, studentId);
            }
            if (overallStatus != null) {
                wrapper.eq(ThesisPreDefense::getOverallStatus, overallStatus);
            }

            IPage<ThesisPreDefense> result = thesisPreDefenseService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/preDefense/record")
    public String recordDefenseResult(@RequestParam Long id,
                                    @RequestParam Integer result,
                                    @RequestParam(required = false) String comment,
                                    @RequestParam(required = false) String qaRecord) {
        try {
            boolean success = thesisPreDefenseService.recordDefenseResult(id, result, comment, qaRecord);
            return success ? jsonReturn.returnSuccess("记录成功") : jsonReturn.returnFailed("记录失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/preDefense/mentor/approve")
    public String preDefenseMentorApprove(@RequestParam Long id,
                                            @RequestParam Integer status,
                                            @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisPreDefenseService.mentorApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/preDefense/secretary/approve")
    public String preDefenseSecretaryApprove(@RequestParam Long id,
                                               @RequestParam Integer status,
                                               @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisPreDefenseService.secretaryApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnFailed("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @PostMapping("/preDefense/dean/approve")
    public String preDefenseDeanApprove(@RequestParam Long id,
                                              @RequestParam Integer status,
                                              @RequestParam(required = false) String comment) {
        try {
            boolean success = thesisPreDefenseService.deanApprove(id, status, comment);
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
            // 条件1：已完成开题报告且通过
            // 条件2：已完成中期检查且通过
            // 条件3：已完成预答辩且通过
            // 条件4：课程学分达标

            LambdaQueryWrapper<ThesisProposal> proposalWrapper = new LambdaQueryWrapper<>();
            proposalWrapper.eq(ThesisProposal::getStudentId, studentId);
            proposalWrapper.eq(ThesisProposal::getOverallStatus, 4); // 4表示已通过

            LambdaQueryWrapper<ThesisMidterm> midtermWrapper = new LambdaQueryWrapper<>();
            midtermWrapper.eq(ThesisMidterm::getStudentId, studentId);
            midtermWrapper.eq(ThesisMidterm::getOverallStatus, 4); // 4表示已通过

            LambdaQueryWrapper<ThesisPreDefense> preDefenseWrapper = new LambdaQueryWrapper<>();
            preDefenseWrapper.eq(ThesisPreDefense::getStudentId, studentId);
            preDefenseWrapper.eq(ThesisPreDefense::getOverallStatus, 4); // 4表示已通过

            boolean proposalPassed = thesisProposalService.count(proposalWrapper) > 0;
            boolean midtermPassed = thesisMidtermService.count(midtermWrapper) > 0;
            boolean preDefensePassed = thesisPreDefenseService.count(preDefenseWrapper) > 0;

            // 检查学分达标情况（简化实现，实际应检查培养方案）
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

    @PostMapping("/degree/defense/record")
    public String recordDefenseResult(@RequestParam Long id,
                                   @RequestParam Integer result,
                                   @RequestParam Double score,
                                   @RequestParam(required = false) String comment,
                                   @RequestParam(required = false) String qaRecord) {
        try {
            boolean success = degreeApplicationService.recordDefenseResult(id, result, score, comment, qaRecord);
            return success ? jsonReturn.returnSuccess("记录成功") : jsonReturn.returnFailed("记录失败");
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
}
