package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.common.enums.ApprovalStatus;
import com.jameshao.gp22023237.common.enums.ProcessType;
import com.jameshao.gp22023237.mapper.DegreeApplicationMapper;
import com.jameshao.gp22023237.po.DegreeApplication;
import com.jameshao.gp22023237.po.ThesisMain;
import com.jameshao.gp22023237.service.AcademicSubmissionService;
import com.jameshao.gp22023237.service.DegreeApplicationService;
import com.jameshao.gp22023237.service.GraduationAuditService;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.SystemConfigService;
import com.jameshao.gp22023237.service.ThesisMainService;
import com.jameshao.gp22023237.service.ThesisProcessRecordService;
import com.jameshao.gp22023237.service.UserService;
import com.jameshao.gp22023237.utils.CurrentUserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DegreeApplicationServiceImpl extends ServiceImpl<DegreeApplicationMapper, DegreeApplication>
        implements DegreeApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(DegreeApplicationServiceImpl.class);

    @Autowired
    private ThesisMainService thesisMainService;

    @Autowired
    private ThesisProcessRecordService thesisProcessRecordService;

    @Autowired
    private GraduationAuditService graduationAuditService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private AcademicSubmissionService academicSubmissionService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitApplication(DegreeApplication application) {
        // === 安全修复：从当前登录用户获取studentId，忽略前端传入 ===
        Long currentUserId = CurrentUserUtil.getCurrentUserId();
        com.jameshao.gp22023237.po.Student student = studentService.getByUserId(currentUserId);
        if (student == null) {
            throw new IllegalStateException("当前用户不是学生，无法提交学位申请");
        }
        Long studentId = student.getId();
        application.setStudentId(studentId);
        application.setStudentName(student.getStudentName());
        application.setStudentNo(student.getStudentNo());

        // === 前置条件校验 ===
        // 1. 检查论文主记录存在
        ThesisMain thesisMain = thesisMainService.getByStudentId(studentId);
        if (thesisMain == null) {
            throw new IllegalStateException("未找到论文记录，无法提交学位申请");
        }

        // 2. 检查毕业论文环节(processType=7)已通过
        boolean thesisPassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.FINAL_THESIS.getCode());
        if (!thesisPassed) {
            throw new IllegalStateException("毕业论文尚未通过，无法提交学位申请");
        }

        // 3. 检查学分是否达标
        BigDecimal totalCredits = graduationAuditService.calculateTotalCredits(studentId);
        String requiredCreditsStr = systemConfigService.getConfigValue("graduation_required_credits");
        BigDecimal requiredCredits = requiredCreditsStr != null ? new BigDecimal(requiredCreditsStr) : new BigDecimal("30");
        if (totalCredits.compareTo(requiredCredits) < 0) {
            throw new IllegalStateException("学分未达标（已修" + totalCredits + "/" + requiredCredits + "），无法提交学位申请");
        }

        // === BUG-7修复：4. 检查实践条件是否满足（与checkEligibility一致） ===
        long practiceCount = academicSubmissionService.count(
                new LambdaQueryWrapper<com.jameshao.gp22023237.po.AcademicSubmission>()
                        .eq(com.jameshao.gp22023237.po.AcademicSubmission::getStudentId, studentId)
                        .eq(com.jameshao.gp22023237.po.AcademicSubmission::getContentType, 3)
                        .eq(com.jameshao.gp22023237.po.AcademicSubmission::getApprovalStatus, 4)
                        .eq(com.jameshao.gp22023237.po.AcademicSubmission::getIsDeleted, 0)
        );
        String requiredPracticeStr = systemConfigService.getConfigValue("graduation_required_practice");
        int requiredPractice = requiredPracticeStr != null ? Integer.parseInt(requiredPracticeStr) : 1;
        if (practiceCount < requiredPractice) {
            throw new IllegalStateException("实践项目未满足要求（已通过" + practiceCount + "/" + requiredPractice + "），无法提交学位申请");
        }

        // === 重复提交校验：检查是否存在"未拒绝"的申请（被驳回的允许重新提交） ===
        DegreeApplication rejectedApp = null;
        if (studentId != null) {
            // 查找是否有进行中的申请（未拒绝的）
            LambdaQueryWrapper<DegreeApplication> activeWrapper = new LambdaQueryWrapper<>();
            activeWrapper.eq(DegreeApplication::getStudentId, studentId);
            activeWrapper.ne(DegreeApplication::getCommitteeStatus, ApprovalStatus.REJECTED.getCode());
            long activeCount = count(activeWrapper);
            if (activeCount > 0) {
                throw new IllegalStateException("该学生已有进行中的学位申请，请勿重复提交");
            }
            // 查找是否有被驳回的旧申请，用于UPDATE而非INSERT
            LambdaQueryWrapper<DegreeApplication> rejectedWrapper = new LambdaQueryWrapper<>();
            rejectedWrapper.eq(DegreeApplication::getStudentId, studentId);
            rejectedWrapper.eq(DegreeApplication::getCommitteeStatus, ApprovalStatus.REJECTED.getCode());
            rejectedWrapper.orderByDesc(DegreeApplication::getSubmitTime);
            rejectedWrapper.last("LIMIT 1");
            rejectedApp = getOne(rejectedWrapper, false);
        }

        if (rejectedApp != null) {
            // 驳回后重提：UPDATE旧记录，避免唯一索引冲突
            rejectedApp.setDegreeType(application.getDegreeType());
            rejectedApp.setThesisTitle(application.getThesisTitle());
            rejectedApp.setThesisAttachment(application.getThesisAttachment());
            rejectedApp.setDefenseTime(application.getDefenseTime());
            rejectedApp.setDefenseLocation(application.getDefenseLocation());
            rejectedApp.setCommitteeChair(application.getCommitteeChair());
            rejectedApp.setCommitteeMembers(application.getCommitteeMembers());
            rejectedApp.setAttachmentPath(application.getAttachmentPath());
            rejectedApp.setSubmitTime(new Date());
            rejectedApp.setCommitteeStatus(ApprovalStatus.UNAPPROVED.getCode());
            rejectedApp.setCommitteeComment(null);
            rejectedApp.setCommitteeApproverId(null);
            rejectedApp.setCommitteeApproveTime(null);
            rejectedApp.setDefenseResult(0);
            rejectedApp.setDefenseScore(null);
            rejectedApp.setDefenseCommitteeComment(null);
            rejectedApp.setQaRecord(null);
            rejectedApp.setDegreeGranted(0);
            rejectedApp.setCertificateNo(null);
            rejectedApp.setDegreeGrantDate(null);
            rejectedApp.setUpdateTime(new Date());
            return updateById(rejectedApp);
        } else {
            // 首次提交：INSERT新记录
            application.setSubmitTime(new Date());
            application.setCommitteeStatus(ApprovalStatus.UNAPPROVED.getCode());
            application.setDegreeGranted(0);
            boolean result = save(application);
            // 6.1 通知：学位申请提交 → 通知分委会/院长
            if (result) {
                try {
                    // 通知分委会主席(roleId=3)和分管院长(roleId=2)
                    List<com.jameshao.gp22023237.po.User> approvers = userService.list(
                        new LambdaQueryWrapper<com.jameshao.gp22023237.po.User>()
                            .in(com.jameshao.gp22023237.po.User::getRoleId, 2, 3)
                            .eq(com.jameshao.gp22023237.po.User::getStatus, 1));
                    List<Long> approverUserIds = approvers.stream()
                        .map(com.jameshao.gp22023237.po.User::getId).collect(Collectors.toList());
                    if (!approverUserIds.isEmpty()) {
                        String studentName = student.getStudentName() != null ? student.getStudentName() : "学生";
                        noticeService.createAndPushToUsers(approverUserIds, "学位申请通知",
                            "学生" + studentName + "提交了学位申请，请审批", "1");
                    }
                } catch (Exception e) {
                    logger.warn("学位申请提交通知推送失败: {}", e.getMessage());
                }
            }
            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean committeeApprove(Long id, Integer status, String comment, Long approverId) {
        DegreeApplication application = getById(id);
        if (application == null) {
            throw new IllegalArgumentException("未找到记录");
        }

        // === 角色校验：仅分管院长(roleId=2)或学位分委会主席(roleId=3)或超级管理员(roleId=1)可审批 ===
        Integer currentRoleId = CurrentUserUtil.getCurrentRoleId();
        if (currentRoleId == null ||
                (currentRoleId != 1 && currentRoleId != 2 && currentRoleId != 3)) {
            throw new IllegalStateException("仅分管院长或学位分委会主席可执行此操作");
        }

        // 校验当前分委会状态是否允许审批
        if (application.getCommitteeStatus() != null &&
            application.getCommitteeStatus() != ApprovalStatus.UNAPPROVED.getCode()) {
            throw new IllegalStateException("该申请已由分委会审批过");
        }

        application.setCommitteeStatus(status);
        application.setCommitteeComment(comment);
        // === 安全修复：审批人ID从当前登录用户获取，忽略前端传入 ===
        application.setCommitteeApproverId(CurrentUserUtil.getCurrentUserId());
        application.setCommitteeApproveTime(new Date());

        boolean result = updateById(application);
        // 6.2 通知：分委会审批 → 通知申请学生
        if (result) {
            try {
                Long studentUserId = noticeService.getStudentUserId(application.getStudentId());
                if (studentUserId != null) {
                    String actionText = ApprovalStatus.APPROVED.getCode().equals(status) ? "通过" : "驳回";
                    String content = "学位申请已" + actionText;
                    if (comment != null && !comment.trim().isEmpty()) {
                        content += "，意见：" + comment;
                    }
                    noticeService.createAndPush("学位审批通知", content, "1", studentUserId);
                }
            } catch (Exception e) {
                logger.warn("学位审批通知推送失败: {}", e.getMessage());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resubmitApplication(DegreeApplication application) {
        // === BUG-8修复：允许被驳回的申请重新提交 ===
        if (application.getId() == null) {
            throw new IllegalArgumentException("申请ID不能为空");
        }
        DegreeApplication existing = getById(application.getId());
        if (existing == null) {
            throw new IllegalArgumentException("未找到学位申请记录");
        }
        // 校验当前状态必须为"已拒绝"
        if (!ApprovalStatus.REJECTED.getCode().equals(existing.getCommitteeStatus())) {
            throw new IllegalStateException("仅被驳回的申请可以重新提交");
        }
        // 安全校验：只有申请人本人可以重提
        Long currentUserId = CurrentUserUtil.getCurrentUserId();
        com.jameshao.gp22023237.po.Student student = studentService.getByUserId(currentUserId);
        if (student == null || !student.getId().equals(existing.getStudentId())) {
            throw new IllegalStateException("仅申请人本人可以重新提交");
        }

        // 更新申请内容，重置审批状态
        existing.setDegreeType(application.getDegreeType());
        existing.setThesisTitle(application.getThesisTitle());
        existing.setThesisAttachment(application.getThesisAttachment());
        existing.setDefenseTime(application.getDefenseTime());
        existing.setDefenseLocation(application.getDefenseLocation());
        existing.setCommitteeChair(application.getCommitteeChair());
        existing.setCommitteeMembers(application.getCommitteeMembers());
        existing.setAttachmentPath(application.getAttachmentPath());
        existing.setSubmitTime(new Date());
        existing.setCommitteeStatus(ApprovalStatus.UNAPPROVED.getCode());
        existing.setCommitteeComment(null);
        existing.setCommitteeApproverId(null);
        existing.setCommitteeApproveTime(null);
        existing.setDefenseResult(0);
        existing.setDefenseScore(null);
        existing.setDefenseCommitteeComment(null);
        existing.setQaRecord(null);
        existing.setDegreeGranted(0);
        existing.setCertificateNo(null);
        existing.setDegreeGrantDate(null);
        existing.setUpdateTime(new Date());
        boolean result = updateById(existing);
        // 6.5 通知：重新提交申请 → 通知分委会/院长
        if (result) {
            try {
                List<com.jameshao.gp22023237.po.User> approvers = userService.list(
                    new LambdaQueryWrapper<com.jameshao.gp22023237.po.User>()
                        .in(com.jameshao.gp22023237.po.User::getRoleId, 2, 3)
                        .eq(com.jameshao.gp22023237.po.User::getStatus, 1));
                List<Long> approverUserIds = approvers.stream()
                    .map(com.jameshao.gp22023237.po.User::getId).collect(Collectors.toList());
                if (!approverUserIds.isEmpty()) {
                    com.jameshao.gp22023237.po.Student stu = studentService.getById(existing.getStudentId());
                    String studentName = stu != null ? stu.getStudentName() : "学生";
                    noticeService.createAndPushToUsers(approverUserIds, "学位申请通知",
                        "学生" + studentName + "重新提交了学位申请，请审批", "1");
                }
            } catch (Exception e) {
                logger.warn("学位重新提交通知推送失败: {}", e.getMessage());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean grantDegree(Long id, String certificateNo) {
        DegreeApplication application = getById(id);
        if (application == null) {
            throw new IllegalArgumentException("未找到记录");
        }

        // 检查学位是否已授予
        if (application.getDegreeGranted() != null && application.getDegreeGranted() == 1) {
            throw new IllegalStateException("该学生已被授予学位");
        }

        // 检查分委会是否已审批通过
        if (!ApprovalStatus.APPROVED.getCode().equals(application.getCommitteeStatus())) {
            throw new IllegalStateException("分委会尚未审批通过，不能授予学位");
        }

        // 检查答辩结果：从thesis_process_record读取（统一数据源）
        ThesisMain thesisMain = thesisMainService.getByStudentId(application.getStudentId());
        if (thesisMain == null) {
            throw new IllegalStateException("未找到论文记录，不能授予学位");
        }
        boolean thesisPassed = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.FINAL_THESIS.getCode());
        if (!thesisPassed) {
            throw new IllegalStateException("毕业论文未通过，不能授予学位");
        }

        // === BUG-5修复：检查答辩是否通过 ===
        if (application.getDefenseResult() == null || application.getDefenseResult() != 1) {
            throw new IllegalStateException("答辩未通过，不能授予学位");
        }

        // === BUG-11修复：证书编号必填校验 ===
        if (certificateNo == null || certificateNo.trim().isEmpty()) {
            throw new IllegalStateException("证书编号不能为空");
        }

        // 检查证书编号唯一性
        if (certificateNo != null && !certificateNo.trim().isEmpty()) {
            LambdaQueryWrapper<DegreeApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DegreeApplication::getCertificateNo, certificateNo);
            wrapper.ne(DegreeApplication::getId, id);
            long count = count(wrapper);
            if (count > 0) {
                throw new IllegalStateException("证书编号已存在");
            }
        }

        application.setDegreeGranted(1);
        application.setCertificateNo(certificateNo);
        application.setDegreeGrantDate(new Date());

        boolean result = updateById(application);

        // === 学位授予成功后自动触发毕业审核（独立事务，失败不影响学位授予） ===
        if (result) {
            // 6.3 通知：学位授予 → 通知学生
            try {
                Long studentUserId = noticeService.getStudentUserId(application.getStudentId());
                if (studentUserId != null) {
                    noticeService.createAndPush("学位授予通知",
                        "恭喜！您已被授予学位，证书编号：" + certificateNo, "1", studentUserId);
                }
            } catch (Exception e) {
                logger.warn("学位授予通知推送失败: {}", e.getMessage());
            }
            try {
                graduationAuditService.autoAuditInNewTransaction(application.getStudentId());
                logger.info("学位授予后自动触发毕业审核成功，学生ID: {}", application.getStudentId());
            } catch (Exception e) {
                logger.warn("学位授予后自动触发毕业审核失败，不影响学位授予: {}", e.getMessage());
            }
        }

        return result;
    }

    @Override
    public DegreeApplication getDetailWithStudentInfo(Long id) {
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDefenseResult(Long id, Integer defenseResult, Double defenseScore,
                                       String defenseCommitteeComment, String qaRecord) {
        DegreeApplication application = getById(id);
        if (application == null) {
            throw new IllegalArgumentException("未找到学位申请记录");
        }
        if (application.getDefenseResult() != null && application.getDefenseResult() != 0) {
            throw new IllegalStateException("该申请已有答辩结果，不允许重复录入");
        }
        if (defenseResult == null) {
            throw new IllegalArgumentException("答辩结果不能为空");
        }

        // === BUG-9修复：校验答辩是否已安排（答辩时间和答辩地点必须已填写） ===
        if (application.getDefenseTime() == null) {
            throw new IllegalStateException("答辩尚未安排（缺少答辩时间），不能录入答辩结果");
        }
        if (application.getDefenseLocation() == null || application.getDefenseLocation().trim().isEmpty()) {
            throw new IllegalStateException("答辩尚未安排（缺少答辩地点），不能录入答辩结果");
        }

        application.setDefenseResult(defenseResult);
        application.setDefenseScore(defenseScore);
        application.setDefenseCommitteeComment(defenseCommitteeComment);
        application.setQaRecord(qaRecord);
        application.setUpdateTime(new Date());

        boolean result = updateById(application);
        // 6.4 通知：答辩结果录入 → 通知学生
        if (result) {
            try {
                Long studentUserId = noticeService.getStudentUserId(application.getStudentId());
                if (studentUserId != null) {
                    String resultText = defenseResult == 1 ? "通过" : "不通过";
                    String content = "答辩结果已录入：" + resultText;
                    if (defenseScore != null) {
                        content += "，得分：" + defenseScore;
                    }
                    noticeService.createAndPush("答辩结果通知", content, "1", studentUserId);
                }
            } catch (Exception e) {
                logger.warn("答辩结果通知推送失败: {}", e.getMessage());
            }
        }
        return result;
    }
}
