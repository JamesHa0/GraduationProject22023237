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
import com.jameshao.gp22023237.service.GraduationAuditService;
import com.jameshao.gp22023237.service.MentorStudentService;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.AcademicSubmissionService;
import com.jameshao.gp22023237.service.SystemConfigService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.service.ThesisMainService;
import com.jameshao.gp22023237.service.ThesisProcessRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ThesisManagementController.class);

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

    @Autowired
    private GraduationAuditService graduationAuditService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private AcademicSubmissionService academicSubmissionService;

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
    public String updateThesisMain(@RequestBody Map<String, Object> params) {
        try {
            Object idObj = params.get("id");
            if (idObj == null) {
                return jsonReturn.returnError("论文ID不能为空");
            }
            Long id = Long.valueOf(idObj.toString());
            ThesisMain thesisMain = thesisMainService.getById(id);
            if (thesisMain == null) {
                return jsonReturn.returnError("未找到论文记录");
            }
            // 字段级权限控制：只允许修改thesisTitle和thesisFinalUrl（安全原则）
            if (params.containsKey("thesisTitle") && params.get("thesisTitle") != null) {
                thesisMain.setThesisTitle(String.valueOf(params.get("thesisTitle")));
            }
            if (params.containsKey("thesisFinalUrl") && params.get("thesisFinalUrl") != null) {
                thesisMain.setThesisFinalUrl(String.valueOf(params.get("thesisFinalUrl")));
            }
            boolean success = thesisMainService.updateById(thesisMain);
            return success ? jsonReturn.returnSuccess("更新成功") : jsonReturn.returnError("更新失败");
        } catch (NumberFormatException e) {
            return jsonReturn.returnError("论文ID格式错误");
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
            // 2.9 通知：论文归档 → 通知学生
            if (success) {
                try {
                    ThesisMain thesisMain = thesisMainService.getById(id);
                    if (thesisMain != null && thesisMain.getStudentId() != null) {
                        Long studentUserId = noticeService.getStudentUserId(thesisMain.getStudentId());
                        if (studentUserId != null) {
                            noticeService.createAndPush("论文归档通知", "您的论文已归档", "1", studentUserId);
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("论文归档通知推送失败: {}", ex.getMessage());
                }
            }
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
            java.util.List<Number> rawIds = (java.util.List<Number>) params.get("ids");
            java.util.List<Long> ids = new ArrayList<>();
            if (rawIds != null) {
                for (Number n : rawIds) {
                    ids.add(n.longValue());
                }
            }
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
                                 @RequestParam(required = false) String thesisId,
                                 @RequestParam(required = false) Integer processType,
                                 @RequestParam(required = false) Integer processStatus) {
        try {
            Page<ThesisProcessRecord> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ThesisProcessRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ThesisProcessRecord::getSubmitTime);

            // 支持单个 thesisId 或逗号分隔的多个 thesisId
            if (thesisId != null && !thesisId.trim().isEmpty()) {
                List<Long> thesisIdList = Arrays.stream(thesisId.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
                if (!thesisIdList.isEmpty()) {
                    wrapper.in(ThesisProcessRecord::getThesisId, thesisIdList);
                }
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
                                     @RequestParam(required = false) String comment) {
        try {
            Long approverId = com.jameshao.gp22023237.utils.CurrentUserUtil.getCurrentUserId();
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
                                    @RequestParam(required = false) String comment) {
        try {
            Long approverId = com.jameshao.gp22023237.utils.CurrentUserUtil.getCurrentUserId();
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
                               @RequestParam(required = false) String comment) {
        try {
            Long approverId = com.jameshao.gp22023237.utils.CurrentUserUtil.getCurrentUserId();
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
                    "creditsQualified", false,
                    "thesisPassed", false,
                    "practicePassed", false,
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

            // 实际校验学分是否达标
            boolean creditsQualified = false;
            try {
                java.math.BigDecimal totalCredits = graduationAuditService.calculateTotalCredits(studentId);
                String requiredCreditsStr = systemConfigService.getConfigValue("graduation_required_credits");
                java.math.BigDecimal requiredCredits = requiredCreditsStr != null
                        ? new java.math.BigDecimal(requiredCreditsStr) : new java.math.BigDecimal("30");
                creditsQualified = totalCredits.compareTo(requiredCredits) >= 0;
            } catch (Exception e) {
                // 学分校验异常时默认不通过
                creditsQualified = false;
            }

            // 论文各环节综合是否全部通过
            boolean thesisPassed = topicPassed && taskBookPassed && proposalPassed && midtermPassed && draftPassed && defenseDraftPassed;

            // 实践条件检查（与 DegreeApplicationServiceImpl 保持一致）
            boolean practicePassed = false;
            try {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.jameshao.gp22023237.po.AcademicSubmission> practiceWrapper =
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                practiceWrapper.eq(com.jameshao.gp22023237.po.AcademicSubmission::getStudentId, studentId)
                        .eq(com.jameshao.gp22023237.po.AcademicSubmission::getContentType, 3)
                        .eq(com.jameshao.gp22023237.po.AcademicSubmission::getApprovalStatus, 4)
                        .eq(com.jameshao.gp22023237.po.AcademicSubmission::getIsDeleted, 0);
                long practiceCount = academicSubmissionService.count(practiceWrapper);
                String requiredPracticeStr = systemConfigService.getConfigValue("graduation_required_practice");
                int requiredPractice = requiredPracticeStr != null ? Integer.parseInt(requiredPracticeStr) : 1;
                practicePassed = practiceCount >= requiredPractice;
            } catch (Exception e) {
                practicePassed = false;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("eligible", thesisPassed && creditsQualified && practicePassed);
            // 顶层字段供前端直接读取
            result.put("creditsQualified", creditsQualified);
            result.put("thesisPassed", thesisPassed);
            result.put("practicePassed", practicePassed);
            // 详细条件（保留兼容）
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
     * 获取导师关联的论文ID列表（公共方法，消除重复代码）
     */
    private List<Long> getSupervisorThesisIds(Long supervisorId) {
        LambdaQueryWrapper<MentorStudent> msWrapper = new LambdaQueryWrapper<>();
        msWrapper.eq(MentorStudent::getMentorId, supervisorId);
        msWrapper.eq(MentorStudent::getStudentStatus, 1);
        List<MentorStudent> mentorStudents = mentorStudentService.list(msWrapper);
        List<Long> studentIds = mentorStudents.stream()
                .map(MentorStudent::getStudentId).collect(Collectors.toList());

        if (studentIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ThesisMain> thesisList = thesisMainService.list(
                new LambdaQueryWrapper<ThesisMain>().in(ThesisMain::getStudentId, studentIds));
        return thesisList.stream().map(ThesisMain::getId).collect(Collectors.toList());
    }

    /**
     * 获取我的学生列表
     */
    @GetMapping("/supervisor/students")
    public String getSupervisorStudents(@RequestParam Long supervisorId) {
        try {
            // 获取导师关联的学生ID列表
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
            List<Long> thesisIds = getSupervisorThesisIds(supervisorId);
            if (thesisIds.isEmpty()) {
                Page<ThesisProcessRecord> emptyPage = new Page<>(pageNum, pageSize);
                emptyPage.setRecords(new ArrayList<>());
                return jsonReturn.returnSuccess(emptyPage);
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
            Object thesisIdObj = params.get("thesisId");
            if (thesisIdObj == null) {
                return jsonReturn.returnError("thesisId不能为空");
            }
            Long thesisId = Long.valueOf(thesisIdObj.toString());
            String topicName = params.get("topicName") != null ? String.valueOf(params.get("topicName")) : null;
            String topicDesc = params.get("topicDesc") != null ? String.valueOf(params.get("topicDesc")) : null;

            // 更新论文主表题目
            ThesisMain thesisMain = thesisMainService.getById(thesisId);
            if (thesisMain == null) {
                return jsonReturn.returnError("未找到论文记录");
            }
            thesisMain.setThesisTitle(topicName);
            thesisMainService.updateById(thesisMain);

            // 使用fastjson构建contentExtend，避免JSON注入
            com.alibaba.fastjson.JSONObject ext = new com.alibaba.fastjson.JSONObject();
            if (topicDesc != null && !topicDesc.isEmpty()) {
                ext.put("topicDesc", topicDesc);
            }
            ext.put("topicName", topicName);
            ext.put("source", "assigned");

            // 走统一提交流程，由导师指定课题视为导师已审批通过
            ThesisProcessRecord record = new ThesisProcessRecord();
            record.setThesisId(thesisId);
            record.setProcessType(ProcessType.TOPIC.getCode());
            record.setContentExtend(ext.toJSONString());
            // 通过submitProcess统一流程，确保version自动计算、ProcessConfig校验等
            boolean success = thesisProcessRecordService.submitProcess(record);

            // 导师指定课题后，自动通过导师审批
            if (success) {
                ThesisProcessRecord latestRecord = thesisProcessRecordService.getLatestRecord(thesisId, ProcessType.TOPIC.getCode());
                if (latestRecord != null && latestRecord.getSupervisorStatus() != null
                        && latestRecord.getSupervisorStatus() == com.jameshao.gp22023237.common.enums.ApprovalStatus.UNAPPROVED.getCode()) {
                    // 获取当前导师ID（从thesisMain获取supervisorId）
                    thesisProcessRecordService.supervisorApprove(latestRecord.getId(),
                            com.jameshao.gp22023237.common.enums.ApprovalStatus.APPROVED.getCode(),
                            "导师指定课题，自动通过", thesisMain.getSupervisorId());
                }
            }

            // 2.6 通知：导师指定课题 → 通知学生
            notifyStudentThesisAction(thesisId, "课题指定通知", "导师为您指定了课题：" + topicName);

            return jsonReturn.returnSuccess("指定课题成功");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 通知学生论文相关操作（导师端操作通知）
     */
    private void notifyStudentThesisAction(Long thesisId, String title, String content) {
        try {
            ThesisMain thesisMain = thesisMainService.getById(thesisId);
            if (thesisMain != null && thesisMain.getStudentId() != null) {
                Long studentUserId = noticeService.getStudentUserId(thesisMain.getStudentId());
                if (studentUserId != null) {
                    noticeService.createAndPush(title, content, "1", studentUserId);
                }
            }
        } catch (Exception e) {
            logger.warn("论文操作通知推送失败: {}", e.getMessage());
        }
    }

    // ==================== 原有辅助方法 ====================

    /**
     * 获取选题修改申请列表
     */
    @GetMapping("/supervisor/topic-modification")
    public String getTopicModifications(@RequestParam Long supervisorId,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            List<Long> thesisIds = getSupervisorThesisIds(supervisorId);

            if (thesisIds.isEmpty()) {
                Page<ThesisProcessRecord> emptyPage = new Page<>(pageNum, pageSize);
                emptyPage.setRecords(new ArrayList<>());
                return jsonReturn.returnSuccess(emptyPage);
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
                                            @RequestParam(required = false) String comment) {
        try {
            Long approverId = com.jameshao.gp22023237.utils.CurrentUserUtil.getCurrentUserId();
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
                    } catch (Exception ex) {
                        logger.warn("解析contentExtend更新题目失败: {}", ex.getMessage());
                    }
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
            List<Long> thesisIds = getSupervisorThesisIds(supervisorId);
            if (thesisIds.isEmpty()) {
                Page<ThesisProcessRecord> emptyPage = new Page<>(pageNum, pageSize);
                emptyPage.setRecords(new ArrayList<>());
                return jsonReturn.returnSuccess(emptyPage);
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
            Object thesisIdObj = params.get("thesisId");
            if (thesisIdObj == null) {
                return jsonReturn.returnError("thesisId不能为空");
            }
            Long thesisId = Long.valueOf(thesisIdObj.toString());
            String background = params.get("background") != null ? String.valueOf(params.get("background")) : null;
            String mainTask = params.get("mainTask") != null ? String.valueOf(params.get("mainTask")) : null;
            String schedule = params.get("schedule") != null ? String.valueOf(params.get("schedule")) : null;
            String references = params.get("references") != null ? String.valueOf(params.get("references")) : null;

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

            // 走统一提交流程，确保version自动计算、ProcessConfig校验等
            boolean success = thesisProcessRecordService.submitProcess(record);

            // 任务书由导师创建，自动通过导师审批
            if (success) {
                ThesisProcessRecord latestRecord = thesisProcessRecordService.getLatestRecord(thesisId, ProcessType.TASK_BOOK.getCode());
                if (latestRecord != null && latestRecord.getSupervisorStatus() != null
                        && latestRecord.getSupervisorStatus() == com.jameshao.gp22023237.common.enums.ApprovalStatus.UNAPPROVED.getCode()) {
                    ThesisMain thesisMain = thesisMainService.getById(thesisId);
                    Long supervisorId = thesisMain != null ? thesisMain.getSupervisorId() : null;
                    thesisProcessRecordService.supervisorApprove(latestRecord.getId(),
                            com.jameshao.gp22023237.common.enums.ApprovalStatus.APPROVED.getCode(),
                            "导师创建任务书，自动通过", supervisorId);
                }
            }

            // 2.7 通知：导师创建任务书 → 通知学生
            if (success) {
                notifyStudentThesisAction(thesisId, "任务书通知", "导师已创建任务书，请查看");
            }

            return success ? jsonReturn.returnSuccess("创建任务书成功") : jsonReturn.returnError("创建失败");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return jsonReturn.returnError(e.getMessage());
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
            Object recordIdObj = params.get("id");
            if (recordIdObj == null) {
                return jsonReturn.returnError("记录ID不能为空");
            }
            Long recordId = Long.valueOf(recordIdObj.toString());
            ThesisProcessRecord record = thesisProcessRecordService.getById(recordId);
            if (record == null) {
                return jsonReturn.returnError("未找到记录");
            }
            if (record.getProcessType() != ProcessType.TASK_BOOK.getCode()) {
                return jsonReturn.returnError("该记录不是任务书");
            }

            // 校验任务书状态：只有审批中(APPROVING)和已拒绝(REJECTED)状态允许修改
            Integer processStatus = record.getProcessStatus();
            if (processStatus != null && (processStatus == 3 || processStatus == 5)) {
                return jsonReturn.returnError("已通过或已完成的任务书不允许修改");
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
            List<Long> thesisIds = getSupervisorThesisIds(supervisorId);
            if (thesisIds.isEmpty()) {
                Page<ThesisProcessRecord> emptyPage = new Page<>(pageNum, pageSize);
                emptyPage.setRecords(new ArrayList<>());
                return jsonReturn.returnSuccess(emptyPage);
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
                                       @RequestParam(required = false) String comment) {
        try {
            Long approverId = com.jameshao.gp22023237.utils.CurrentUserUtil.getCurrentUserId();
            boolean success = thesisProcessRecordService.supervisorApprove(id, status, comment, approverId);
            // 2.8 通知：导师填写答辩稿评语 → 通知学生
            if (success) {
                ThesisProcessRecord record = thesisProcessRecordService.getById(id);
                if (record != null) {
                    notifyStudentThesisAction(record.getThesisId(), "答辩稿评语通知", "导师已对答辩稿填写评语");
                }
            }
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
            List<Long> thesisIds = getSupervisorThesisIds(supervisorId);
            if (thesisIds.isEmpty()) {
                Page<ThesisProcessRecord> emptyPage = new Page<>(pageNum, pageSize);
                emptyPage.setRecords(new ArrayList<>());
                return jsonReturn.returnSuccess(emptyPage);
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
                                @RequestParam(required = false) String studentNo,
                                @RequestParam(required = false) Integer degreeGranted,
                                @RequestParam(required = false) Integer committeeStatus,
                                @RequestParam(required = false) Integer defenseResult) {
        try {
            Page<DegreeApplication> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<DegreeApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(DegreeApplication::getSubmitTime);

            if (studentId != null) {
                wrapper.eq(DegreeApplication::getStudentId, studentId);
            }
            if (studentNo != null && !studentNo.isEmpty()) {
                wrapper.like(DegreeApplication::getStudentNo, studentNo);
            }
            if (degreeGranted != null) {
                wrapper.eq(DegreeApplication::getDegreeGranted, degreeGranted);
            }
            if (committeeStatus != null) {
                wrapper.eq(DegreeApplication::getCommitteeStatus, committeeStatus);
            }
            if (defenseResult != null) {
                wrapper.eq(DegreeApplication::getDefenseResult, defenseResult);
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
            boolean success = degreeApplicationService.committeeApprove(id, status, comment, null);
            return success ? jsonReturn.returnSuccess("审批成功") : jsonReturn.returnError("审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 重新提交学位申请（分委驳回后允许重提）
     */
    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/degree/resubmit")
    public String resubmitDegreeApplication(@RequestBody DegreeApplication application) {
        try {
            boolean success = degreeApplicationService.resubmitApplication(application);
            return success ? jsonReturn.returnSuccess("重新提交成功") : jsonReturn.returnError("重新提交失败");
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

    /**
     * 获取学位申请详情
     */
    @GetMapping("/degree/{id}")
    public String getDegreeDetail(@PathVariable Long id) {
        try {
            DegreeApplication application = degreeApplicationService.getDetailWithStudentInfo(id);
            return application != null ? jsonReturn.returnSuccess(application) : jsonReturn.returnError("未找到记录");
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError(e.getMessage());
        }
    }

    /**
     * 录入答辩结果
     */
    @Log(title = "论文管理", businessType = BusinessType.UPDATE)
    @PostMapping("/degree/defense/updateResult")
    public String updateDefenseResult(@RequestBody Map<String, Object> params) {
        try {
            Object idObj = params.get("id");
            if (idObj == null) {
                return jsonReturn.returnError("申请ID不能为空");
            }
            Long id = Long.valueOf(idObj.toString());
            Integer defenseResult = params.get("defenseResult") != null
                    ? Integer.valueOf(params.get("defenseResult").toString()) : null;
            Double defenseScore = params.get("defenseScore") != null
                    ? Double.valueOf(params.get("defenseScore").toString()) : null;
            String defenseCommitteeComment = params.get("defenseCommitteeComment") != null
                    ? params.get("defenseCommitteeComment").toString() : null;
            String qaRecord = params.get("qaRecord") != null
                    ? params.get("qaRecord").toString() : null;

            boolean success = degreeApplicationService.updateDefenseResult(id, defenseResult, defenseScore,
                    defenseCommitteeComment, qaRecord);
            return success ? jsonReturn.returnSuccess("录入成功") : jsonReturn.returnError("录入失败");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return jsonReturn.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return jsonReturn.returnError("录入失败：" + e.getMessage());
        }
    }
}
