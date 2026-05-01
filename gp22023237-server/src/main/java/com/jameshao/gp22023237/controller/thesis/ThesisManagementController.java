package com.jameshao.gp22023237.controller.thesis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.ProcessType;
import com.jameshao.gp22023237.po.DegreeApplication;
import com.jameshao.gp22023237.po.ThesisMain;
import com.jameshao.gp22023237.po.ThesisProcessRecord;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.service.DegreeApplicationService;
import com.jameshao.gp22023237.service.ThesisMainService;
import com.jameshao.gp22023237.service.ThesisProcessRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 学位论文管理控制器
 * 负责处理学位论文全流程管理：论文主记录 → 开题报告 → 中期检查 → 预答辩 → 论文外审 → 正式答辩
 * 支持导师、教学秘书、分管院长三级审批体系
 * 路径前缀: /thesis
 */
@RestController
@RequestMapping("/thesis")
public class ThesisManagementController {

    @Autowired
    private JSONReturn jsonReturn;

    @Autowired
    private ThesisMainService thesisMainService;

    @Autowired
    private ThesisProcessRecordService thesisProcessRecordService;

    @Autowired
    private DegreeApplicationService degreeApplicationService;

    // ==================== 论文主记录管理 ====================

    @GetMapping("/main/list")
    public String getThesisMainList(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) Long studentId,
                                    @RequestParam(required = false) String studentNo,
                                    @RequestParam(required = false) Long supervisorId,
                                    @RequestParam(required = false) Integer finalResult,
                                    @RequestParam(required = false) Integer archiveStatus) {
        try {
            Page<ThesisMain> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisMain> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ThesisMain::getUpdateTime);

            if (studentId != null) {
                wrapper.eq(ThesisMain::getStudentId, studentId);
            }
            if (studentNo != null && !studentNo.isEmpty()) {
                wrapper.like(ThesisMain::getStudentNo, studentNo);
            }
            if (supervisorId != null) {
                wrapper.eq(ThesisMain::getSupervisorId, supervisorId);
            }
            if (finalResult != null) {
                wrapper.eq(ThesisMain::getFinalResult, finalResult);
            }
            if (archiveStatus != null) {
                wrapper.eq(ThesisMain::getArchiveStatus, archiveStatus);
            }

            IPage<ThesisMain> result = thesisMainService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/main/{id}")
    public String getThesisMainDetail(@PathVariable Long id) {
        try {
            ThesisMain thesisMain = thesisMainService.getById(id);
            return thesisMain != null ? jsonReturn.returnSuccess(thesisMain) : jsonReturn.returnError("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/main/student/{studentId}")
    public String getThesisMainByStudent(@PathVariable Long studentId) {
        try {
            ThesisMain thesisMain = thesisMainService.getOrCreateByStudentId(studentId);
            return jsonReturn.returnSuccess(thesisMain);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/main/update")
    public String updateThesisMain(@RequestBody ThesisMain thesisMain) {
        try {
            boolean success = thesisMainService.updateById(thesisMain);
            return success ? jsonReturn.returnSuccess("更新成功") : jsonReturn.returnError("更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/main/archive/{id}")
    public String archiveThesis(@PathVariable Long id) {
        try {
            boolean success = thesisMainService.archiveThesis(id);
            return success ? jsonReturn.returnSuccess("归档成功") : jsonReturn.returnError("归档失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/main/batchArchive")
    public String batchArchiveThesis(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Long> ids = (java.util.List<Long>) params.get("ids");
            if (ids == null || ids.isEmpty()) {
                return jsonReturn.returnError("请选择要归档的记录");
            }
            boolean success = thesisMainService.batchArchiveThesis(ids);
            return success ? jsonReturn.returnSuccess("批量归档成功") : jsonReturn.returnError("批量归档失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/main/statistics")
    public String getThesisStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 总数
            long total = thesisMainService.count();
            stats.put("total", total);

            // 按最终结果统计
            long passedCount = thesisMainService.count(new LambdaQueryWrapper<ThesisMain>()
                    .eq(ThesisMain::getFinalResult, 1));
            long failedCount = thesisMainService.count(new LambdaQueryWrapper<ThesisMain>()
                    .eq(ThesisMain::getFinalResult, 2));
            long ongoingCount = total - passedCount - failedCount;

            stats.put("passedCount", passedCount);
            stats.put("failedCount", failedCount);
            stats.put("ongoingCount", ongoingCount);

            // 按归档状态统计
            long archivedCount = thesisMainService.count(new LambdaQueryWrapper<ThesisMain>()
                    .eq(ThesisMain::getArchiveStatus, 1));
            long unarchivedCount = total - archivedCount;
            stats.put("archivedCount", archivedCount);
            stats.put("unarchivedCount", unarchivedCount);

            // 各流程环节通过率
            Map<String, Object> processStats = new HashMap<>();
            ProcessType[] processTypes = ProcessType.values();
            for (ProcessType pt : processTypes) {
                long processTotal = thesisProcessRecordService.count(new LambdaQueryWrapper<ThesisProcessRecord>()
                        .eq(ThesisProcessRecord::getProcessType, pt.getCode()));
                long processPassed = thesisProcessRecordService.count(new LambdaQueryWrapper<ThesisProcessRecord>()
                        .eq(ThesisProcessRecord::getProcessType, pt.getCode())
                        .in(ThesisProcessRecord::getProcessStatus, 3, 5));
                Map<String, Object> item = new HashMap<>();
                item.put("total", processTotal);
                item.put("passed", processPassed);
                item.put("rate", processTotal > 0 ? Math.round(processPassed * 100.0 / processTotal) : 0);
                processStats.put(pt.getDesc(), item);
            }
            stats.put("processStats", processStats);

            return jsonReturn.returnSuccess(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 论文流程记录管理（开题、中期、预答辩、外审、答辩统一接口）====================

    @Log(title = "论文管理", businessType = BusinessType.INSERT)
    @PostMapping("/process/submit")
    public String submitProcess(@RequestBody ThesisProcessRecord record) {
        try {
            // 如果没有thesisId但有studentId，自动获取或创建论文主记录
            if (record.getThesisId() == null) {
                return jsonReturn.returnError("thesisId不能为空");
            }
            boolean success = thesisProcessRecordService.submitProcess(record);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnError("提交失败");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("提交失败：" + e.getMessage());
        }
    }

    @GetMapping("/process/list")
    public String getProcessList(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) Long thesisId,
                                 @RequestParam(required = false) Integer processType,
                                 @RequestParam(required = false) Integer processStatus) {
        try {
            Page<ThesisProcessRecord> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisProcessRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ThesisProcessRecord::getSubmitTime);

            if (thesisId != null) {
                wrapper.eq(ThesisProcessRecord::getThesisId, thesisId);
            }
            if (processType != null) {
                wrapper.eq(ThesisProcessRecord::getProcessType, processType);
            }
            if (processStatus != null) {
                wrapper.eq(ThesisProcessRecord::getProcessStatus, processStatus);
            }

            IPage<ThesisProcessRecord> result = thesisProcessRecordService.page(page, wrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @GetMapping("/process/{id}")
    public String getProcessDetail(@PathVariable Long id) {
        try {
            ThesisProcessRecord record = thesisProcessRecordService.getById(id);
            return record != null ? jsonReturn.returnSuccess(record) : jsonReturn.returnError("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/process/supervisor/approve")
    public String supervisorApprove(@RequestParam Long id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String comment,
                                     @RequestParam(required = false) Long approverId) {
        try {
            boolean success = thesisProcessRecordService.supervisorApprove(id, status, comment, approverId);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnError("审批失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("审批失败：" + e.getMessage());
        }
    }

    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/process/secretary/approve")
    public String secretaryApprove(@RequestParam Long id,
                                    @RequestParam Integer status,
                                    @RequestParam(required = false) String comment,
                                    @RequestParam(required = false) Long approverId) {
        try {
            boolean success = thesisProcessRecordService.secretaryApprove(id, status, comment, approverId);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnError("审批失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("审批失败：" + e.getMessage());
        }
    }

    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/process/dean/approve")
    public String deanApprove(@RequestParam Long id,
                               @RequestParam Integer status,
                               @RequestParam(required = false) String comment,
                               @RequestParam(required = false) Long approverId) {
        try {
            boolean success = thesisProcessRecordService.deanApprove(id, status, comment, approverId);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnError("审批失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("审批失败：" + e.getMessage());
        }
    }

    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/process/recordResult")
    public String recordReviewResult(@RequestParam Long id,
                                      @RequestParam Integer result,
                                      @RequestParam(required = false) String score,
                                      @RequestParam(required = false) String comment,
                                      @RequestParam(required = false) String qaRecord) {
        try {
            boolean success = thesisProcessRecordService.recordReviewResult(id, result, score, comment, qaRecord);
            return success ? jsonReturn.returnSuccess("记录成功") : jsonReturn.returnError("记录失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("记录失败：" + e.getMessage());
        }
    }

    // ==================== 答辩条件检查 ====================

    @GetMapping("/degree/checkEligibility")
    public String checkDefenseEligibility(@RequestParam Long studentId) {
        try {
            // 获取论文主记录
            ThesisMain thesisMain = thesisMainService.getByStudentId(studentId);
            if (thesisMain == null) {
                return jsonReturn.returnSuccess(Map.of(
                    "eligible", false,
                    "conditions", Map.of(
                        "proposalPassed", false,
                        "midtermPassed", false,
                        "preDefensePassed", false,
                        "creditsQualified", false
                    ),
                    "message", "未找到论文记录"
                ));
            }

            // 检查各环节是否已通过
            boolean proposalPassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.PROPOSAL.getCode());
            boolean midtermPassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.MIDTERM.getCode());
            boolean preDefensePassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.PRE_DEFENSE.getCode());
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

    // ==================== 学位申请管理（保持不变）====================

    @Log(title = "论文管理", businessType = BusinessType.INSERT)
    @PostMapping("/degree/submit")
    public String submitDegreeApplication(@RequestBody DegreeApplication application) {
        try {
            boolean success = degreeApplicationService.submitApplication(application);
            return success ? jsonReturn.returnSuccess("提交成功") : jsonReturn.returnError("提交失败");
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

    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/degree/committee/approve")
    public String committeeApprove(@RequestParam Long id,
                                  @RequestParam Integer status,
                                  @RequestParam(required = false) String comment) {
        try {
            boolean success = degreeApplicationService.committeeApprove(id, status, comment);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnError("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/degree/grant")
    public String grantDegree(@RequestParam Long id,
                             @RequestParam String certificateNo) {
        try {
            boolean success = degreeApplicationService.grantDegree(id, certificateNo);
            return success ? jsonReturn.returnSuccess("学位授予成功") : jsonReturn.returnError("学位授予失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }
}
