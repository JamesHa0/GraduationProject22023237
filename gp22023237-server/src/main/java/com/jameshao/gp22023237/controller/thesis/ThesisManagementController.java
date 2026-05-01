package com.jameshao.gp22023237.controller.thesis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jameshao.gp22023237.common.JSONReturn;
import com.jameshao.gp22023237.common.enums.ProcessType;
import com.jameshao.gp22023237.po.DegreeApplication;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.po.ThesisMain;
import com.jameshao.gp22023237.po.ThesisProcessRecord;
import com.jameshao.gp22023237.annotation.Log;
import com.jameshao.gp22023237.common.enums.BusinessType;
import com.jameshao.gp22023237.service.DegreeApplicationService;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.ThesisMainService;
import com.jameshao.gp22023237.service.ThesisProcessRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学位论文管理控制器
 * 负责处理学位论文全流程管理：选题 → 任务书 → 开题报告 → 中期检查 → 过程稿 → 论文答辩稿 → 毕业论文
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

    @Autowired
    private MentorStudentService mentorStudentService;

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
            boolean topicPassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.TOPIC.getCode());
            boolean taskBookPassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.TASK_BOOK.getCode());
            boolean proposalPassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.PROPOSAL.getCode());
            boolean midtermPassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.MIDTERM.getCode());
            boolean draftPassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.DRAFT.getCode());
            boolean defenseDraftPassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.DEFENSE_DRAFT.getCode());
            boolean creditsQualified = true;

            Map<String, Object> result = new HashMap<>();
            result.put("eligible", topicPassed && taskBookPassed && proposalPassed && midtermPassed && draftPassed && defenseDraftPassed && creditsQualified);
            result.put("conditions", Map.of(
                "topicPassed", topicPassed,
                "taskBookPassed", taskBookPassed,
                "proposalPassed", proposalPassed,
                "midtermPassed", midtermPassed,
                "draftPassed", draftPassed,
                "defenseDraftPassed", defenseDraftPassed,
                "creditsQualified", creditsQualified
            ));

            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    // ==================== 导师端接口 ====================

    /**
     * 获取我的学生列表
     */
    @GetMapping("/supervisor/students")
    public String getSupervisorStudents(@RequestParam Long supervisorId) {
        try {
            LambdaQueryWrapper<MentorStudent> msWrapper = new LambdaQueryWrapper<>();
            msWrapper.eq(MentorStudent::getMentorId, supervisorId);
            msWrapper.eq(MentorStudent::getStudentStatus, 1);
            List<MentorStudent> mentorStudents = mentorStudentService.list(msWrapper);

            List<Long> studentIds = mentorStudents.stream()
                    .map(MentorStudent::getStudentId)
                    .collect(Collectors.toList());

            if (studentIds.isEmpty()) {
                return jsonReturn.returnSuccess(new ArrayList<>());
            }

            // 获取学生的论文主记录
            LambdaQueryWrapper<ThesisMain> thesisWrapper = new LambdaQueryWrapper<>();
            thesisWrapper.in(ThesisMain::getStudentId, studentIds);
            List<ThesisMain> thesisList = thesisMainService.list(thesisWrapper);

            return jsonReturn.returnSuccess(thesisList);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取学生选题列表（导师查看自己学生的选题情况）
     */
    @GetMapping("/supervisor/topic")
    public String getSupervisorTopic(@RequestParam Long supervisorId,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 获取导师的学生ID列表
            LambdaQueryWrapper<MentorStudent> msWrapper = new LambdaQueryWrapper<>();
            msWrapper.eq(MentorStudent::getMentorId, supervisorId);
            msWrapper.eq(MentorStudent::getStudentStatus, 1);
            List<MentorStudent> mentorStudents = mentorStudentService.list(msWrapper);
            List<Long> studentIds = mentorStudents.stream()
                    .map(MentorStudent::getStudentId).collect(Collectors.toList());

            if (studentIds.isEmpty()) {
                return jsonReturn.returnSuccess(new HashMap<>());
            }

            // 获取这些学生的论文ID
            LambdaQueryWrapper<ThesisMain> thesisWrapper = new LambdaQueryWrapper<>();
            thesisWrapper.in(ThesisMain::getStudentId, studentIds);
            List<ThesisMain> thesisList = thesisMainService.list(thesisWrapper);
            List<Long> thesisIds = thesisList.stream().map(ThesisMain::getId).collect(Collectors.toList());

            if (thesisIds.isEmpty()) {
                return jsonReturn.returnSuccess(new HashMap<>());
            }

            // 查询选题记录（processType=1）
            Page<ThesisProcessRecord> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisProcessRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.in(ThesisProcessRecord::getThesisId, thesisIds);
            recordWrapper.eq(ThesisProcessRecord::getProcessType, ProcessType.TOPIC.getCode());
            recordWrapper.orderByDesc(ThesisProcessRecord::getSubmitTime);
            IPage<ThesisProcessRecord> result = thesisProcessRecordService.page(page, recordWrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 指定课题（导师为学生指定课题）
     */
    @Log(title = "论文管理", businessType = BusinessType.INSERT)
    @PostMapping("/supervisor/topic/assign")
    public String assignTopic(@RequestBody Map<String, Object> params) {
        try {
            Long thesisId = Long.valueOf(params.get("thesisId").toString());
            String topicName = (String) params.get("topicName");
            String topicDesc = (String) params.get("topicDesc");

            // 更新论文主表题目
            ThesisMain thesisMain = thesisMainService.getById(thesisId);
            if (thesisMain == null) {
                return jsonReturn.returnError("未找到论文记录");
            }
            thesisMain.setThesisTitle(topicName);
            thesisMainService.updateById(thesisMain);

            // 创建一条导师指定的选题记录
            ThesisProcessRecord record = new ThesisProcessRecord();
            record.setThesisId(thesisId);
            record.setProcessType(ProcessType.TOPIC.getCode());
            record.setProcessStatus(3); // PASSED
            record.setSupervisorStatus(1); // APPROVED
            record.setSecretaryStatus(0);
            record.setDeanStatus(0);
            record.setReviewResult(0);
            record.setSubmitTime(new java.util.Date());
            // 将课题说明存入contentExtend
            if (topicDesc != null && !topicDesc.isEmpty()) {
                record.setContentExtend("{\"topicDesc\":\"" + topicDesc.replace("\"", "\\\"") + "\",\"source\":\"assigned\"}");
            }
            thesisProcessRecordService.save(record);

            return jsonReturn.returnSuccess("指定课题成功");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取选题修改申请列表
     */
    @GetMapping("/supervisor/topic-modification")
    public String getTopicModifications(@RequestParam Long supervisorId,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 获取导师的学生ID列表
            LambdaQueryWrapper<MentorStudent> msWrapper = new LambdaQueryWrapper<>();
            msWrapper.eq(MentorStudent::getMentorId, supervisorId);
            msWrapper.eq(MentorStudent::getStudentStatus, 1);
            List<MentorStudent> mentorStudents = mentorStudentService.list(msWrapper);
            List<Long> studentIds = mentorStudents.stream()
                    .map(MentorStudent::getStudentId).collect(Collectors.toList());

            if (studentIds.isEmpty()) {
                return jsonReturn.returnSuccess(new HashMap<>());
            }

            // 获取论文ID
            List<ThesisMain> thesisList = thesisMainService.list(
                    new LambdaQueryWrapper<ThesisMain>().in(ThesisMain::getStudentId, studentIds));
            List<Long> thesisIds = thesisList.stream().map(ThesisMain::getId).collect(Collectors.toList());

            if (thesisIds.isEmpty()) {
                return jsonReturn.returnSuccess(new HashMap<>());
            }

            // 查询选题修改申请（version > 1 的选题记录即为修改申请）
            Page<ThesisProcessRecord> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisProcessRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.in(ThesisProcessRecord::getThesisId, thesisIds);
            recordWrapper.eq(ThesisProcessRecord::getProcessType, ProcessType.TOPIC.getCode());
            recordWrapper.gt(ThesisProcessRecord::getVersion, 1);
            recordWrapper.orderByDesc(ThesisProcessRecord::getSubmitTime);
            IPage<ThesisProcessRecord> result = thesisProcessRecordService.page(page, recordWrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 审批选题修改申请（复用导师审批接口）
     */
    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/supervisor/topic-modification/approve")
    public String approveTopicModification(@RequestParam Long id,
                                            @RequestParam Integer status,
                                            @RequestParam(required = false) String comment,
                                            @RequestParam Long approverId) {
        try {
            boolean success = thesisProcessRecordService.supervisorApprove(id, status, comment, approverId);
            // 如果通过，更新论文主表题目
            if (success && status == 1) {
                ThesisProcessRecord record = thesisProcessRecordService.getById(id);
                if (record != null && record.getContentExtend() != null) {
                    // 从contentExtend中解析新题目
                    try {
                        com.alibaba.fastjson.JSONObject ext = com.alibaba.fastjson.JSON.parseObject(record.getContentExtend());
                        String newTitle = ext.getString("topicName");
                        if (newTitle != null) {
                            ThesisMain thesisMain = thesisMainService.getById(record.getThesisId());
                            if (thesisMain != null) {
                                thesisMain.setThesisTitle(newTitle);
                                thesisMainService.updateById(thesisMain);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnError("审批失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("审批失败：" + e.getMessage());
        }
    }

    /**
     * 获取学生任务书列表
     */
    @GetMapping("/supervisor/task")
    public String getSupervisorTask(@RequestParam Long supervisorId,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            LambdaQueryWrapper<MentorStudent> msWrapper = new LambdaQueryWrapper<>();
            msWrapper.eq(MentorStudent::getMentorId, supervisorId);
            msWrapper.eq(MentorStudent::getStudentStatus, 1);
            List<MentorStudent> mentorStudents = mentorStudentService.list(msWrapper);
            List<Long> studentIds = mentorStudents.stream()
                    .map(MentorStudent::getStudentId).collect(Collectors.toList());

            if (studentIds.isEmpty()) {
                return jsonReturn.returnSuccess(new HashMap<>());
            }

            List<ThesisMain> thesisList = thesisMainService.list(
                    new LambdaQueryWrapper<ThesisMain>().in(ThesisMain::getStudentId, studentIds));
            List<Long> thesisIds = thesisList.stream().map(ThesisMain::getId).collect(Collectors.toList());

            if (thesisIds.isEmpty()) {
                return jsonReturn.returnSuccess(new HashMap<>());
            }

            Page<ThesisProcessRecord> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisProcessRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.in(ThesisProcessRecord::getThesisId, thesisIds);
            recordWrapper.eq(ThesisProcessRecord::getProcessType, ProcessType.TASK_BOOK.getCode());
            recordWrapper.orderByDesc(ThesisProcessRecord::getSubmitTime);
            IPage<ThesisProcessRecord> result = thesisProcessRecordService.page(page, recordWrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 为学生创建任务书
     */
    @Log(title = "论文管理", businessType = BusinessType.INSERT)
    @PostMapping("/supervisor/task/create")
    public String createTask(@RequestBody Map<String, Object> params) {
        try {
            Long thesisId = Long.valueOf(params.get("thesisId").toString());
            String background = (String) params.get("background");
            String mainTask = (String) params.get("mainTask");
            String schedule = (String) params.get("schedule");
            String references = (String) params.get("references");

            // 构建contentExtend JSON
            com.alibaba.fastjson.JSONObject ext = new com.alibaba.fastjson.JSONObject();
            if (background != null) ext.put("background", background);
            if (mainTask != null) ext.put("mainTask", mainTask);
            if (schedule != null) ext.put("schedule", schedule);
            if (references != null) ext.put("references", references);
            ext.put("source", "supervisor");

            ThesisProcessRecord record = new ThesisProcessRecord();
            record.setThesisId(thesisId);
            record.setProcessType(ProcessType.TASK_BOOK.getCode());
            record.setContentExtend(ext.toJSONString());
            // 任务书由导师创建，导师审批自动通过
            record.setSupervisorStatus(1); // APPROVED
            record.setSecretaryStatus(0); // UNAPPROVED - 等待教学秘书审核
            record.setDeanStatus(0);
            record.setReviewResult(0);
            record.setProcessStatus(1); // APPROVING
            record.setSubmitTime(new java.util.Date());

            boolean success = thesisProcessRecordService.save(record);
            return success ? jsonReturn.returnSuccess("创建任务书成功") : jsonReturn.returnError("创建失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 修改任务书
     */
    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PutMapping("/supervisor/task/update")
    public String updateTask(@RequestBody Map<String, Object> params) {
        try {
            Long recordId = Long.valueOf(params.get("id").toString());
            ThesisProcessRecord record = thesisProcessRecordService.getById(recordId);
            if (record == null) {
                return jsonReturn.returnError("未找到记录");
            }
            if (record.getProcessType() != ProcessType.TASK_BOOK.getCode()) {
                return jsonReturn.returnError("该记录不是任务书");
            }

            // 更新contentExtend
            com.alibaba.fastjson.JSONObject ext = new com.alibaba.fastjson.JSONObject();
            if (params.get("background") != null) ext.put("background", params.get("background"));
            if (params.get("mainTask") != null) ext.put("mainTask", params.get("mainTask"));
            if (params.get("schedule") != null) ext.put("schedule", params.get("schedule"));
            if (params.get("references") != null) ext.put("references", params.get("references"));
            ext.put("source", "supervisor");
            record.setContentExtend(ext.toJSONString());

            boolean success = thesisProcessRecordService.updateById(record);
            return success ? jsonReturn.returnSuccess("修改成功") : jsonReturn.returnError("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 获取学生答辩稿列表
     */
    @GetMapping("/supervisor/defense-draft")
    public String getSupervisorDefenseDraft(@RequestParam Long supervisorId,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            LambdaQueryWrapper<MentorStudent> msWrapper = new LambdaQueryWrapper<>();
            msWrapper.eq(MentorStudent::getMentorId, supervisorId);
            msWrapper.eq(MentorStudent::getStudentStatus, 1);
            List<MentorStudent> mentorStudents = mentorStudentService.list(msWrapper);
            List<Long> studentIds = mentorStudents.stream()
                    .map(MentorStudent::getStudentId).collect(Collectors.toList());

            if (studentIds.isEmpty()) {
                return jsonReturn.returnSuccess(new HashMap<>());
            }

            List<ThesisMain> thesisList = thesisMainService.list(
                    new LambdaQueryWrapper<ThesisMain>().in(ThesisMain::getStudentId, studentIds));
            List<Long> thesisIds = thesisList.stream().map(ThesisMain::getId).collect(Collectors.toList());

            if (thesisIds.isEmpty()) {
                return jsonReturn.returnSuccess(new HashMap<>());
            }

            Page<ThesisProcessRecord> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisProcessRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.in(ThesisProcessRecord::getThesisId, thesisIds);
            recordWrapper.eq(ThesisProcessRecord::getProcessType, ProcessType.DEFENSE_DRAFT.getCode());
            recordWrapper.orderByDesc(ThesisProcessRecord::getSubmitTime);
            IPage<ThesisProcessRecord> result = thesisProcessRecordService.page(page, recordWrapper);
            return jsonReturn.returnSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 填写答辩稿评语（复用导师审批+在comment中记录评语）
     */
    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/supervisor/defense-draft/comment")
    public String commentDefenseDraft(@RequestParam Long id,
                                       @RequestParam Integer status,
                                       @RequestParam(required = false) String comment,
                                       @RequestParam Long approverId) {
        try {
            boolean success = thesisProcessRecordService.supervisorApprove(id, status, comment, approverId);
            return success ? jsonReturn.returnSuccess("操作成功") : jsonReturn.returnError("操作失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("操作失败：" + e.getMessage());
        }
    }

    /**
     * 获取学生最终稿列表
     */
    @GetMapping("/supervisor/thesis-final")
    public String getSupervisorThesisFinal(@RequestParam Long supervisorId,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            LambdaQueryWrapper<MentorStudent> msWrapper = new LambdaQueryWrapper<>();
            msWrapper.eq(MentorStudent::getMentorId, supervisorId);
            msWrapper.eq(MentorStudent::getStudentStatus, 1);
            List<MentorStudent> mentorStudents = mentorStudentService.list(msWrapper);
            List<Long> studentIds = mentorStudents.stream()
                    .map(MentorStudent::getStudentId).collect(Collectors.toList());

            if (studentIds.isEmpty()) {
                return jsonReturn.returnSuccess(new HashMap<>());
            }

            List<ThesisMain> thesisList = thesisMainService.list(
                    new LambdaQueryWrapper<ThesisMain>().in(ThesisMain::getStudentId, studentIds));
            List<Long> thesisIds = thesisList.stream().map(ThesisMain::getId).collect(Collectors.toList());

            if (thesisIds.isEmpty()) {
                return jsonReturn.returnSuccess(new HashMap<>());
            }

            Page<ThesisProcessRecord> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisProcessRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.in(ThesisProcessRecord::getThesisId, thesisIds);
            recordWrapper.eq(ThesisProcessRecord::getProcessType, ProcessType.FINAL_THESIS.getCode());
            recordWrapper.orderByDesc(ThesisProcessRecord::getSubmitTime);
            IPage<ThesisProcessRecord> result = thesisProcessRecordService.page(page, recordWrapper);
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
