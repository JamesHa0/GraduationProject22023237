package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.DTO.GraduationAuditWithDetailsDTO;
import com.jameshao.gp22023237.common.enums.ProcessType;
import com.jameshao.gp22023237.mapper.GraduationAuditMapper;
import com.jameshao.gp22023237.po.*;
import com.jameshao.gp22023237.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class GraduationAuditServiceImpl extends ServiceImpl<GraduationAuditMapper, GraduationAudit>
        implements GraduationAuditService {

    private static final Logger logger = LoggerFactory.getLogger(GraduationAuditServiceImpl.class);

    @Autowired
    private ThesisMainService thesisMainService;

    @Autowired
    private ThesisProcessRecordService thesisProcessRecordService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private AcademicSubmissionService academicSubmissionService;

    @Autowired
    private NoticeService noticeService;

    @Lazy
    @Autowired
    private GraduationAuditService selfProxy;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraduationAudit autoAudit(Long studentId) {
        // 查询或创建审核记录
        GraduationAudit audit = lambdaQuery()
                .eq(GraduationAudit::getStudentId, studentId)
                .one();

        if (audit == null) {
            audit = new GraduationAudit();
            audit.setStudentId(studentId);
            audit.setCreateTime(new Date());
        }

        // === BUG-10修复：保护人工审核结果，已有人工审核通过时不覆盖 ===
        if (audit.getAuditorId() != null && audit.getAuditStatus() != null && audit.getAuditStatus() == 1) {
            logger.info("学生ID={}已有通过的人工审核结果(auditorId={)，跳过自动审核覆盖", studentId, audit.getAuditorId());
            return audit;
        }

        // 初始化三项检查为未审核(0)
        audit.setCreditsPass(0);
        audit.setThesisPass(0);
        audit.setPracticePass(0);

        // === 1. 学分检查：从score表+course表汇总已修学分 ===
        BigDecimal totalCredits = calculateTotalCredits(studentId);
        String requiredCreditsStr = systemConfigService.getConfigValue("graduation_required_credits");
        BigDecimal requiredCredits = requiredCreditsStr != null ? new BigDecimal(requiredCreditsStr) : new BigDecimal("30");

        if (totalCredits.compareTo(requiredCredits) >= 0) {
            audit.setCreditsPass(1); // 通过
        } else {
            audit.setCreditsPass(2); // 未通过
        }

        // === 2. 论文检查：检查毕业论文环节(processType=7)是否通过 ===
        boolean thesisPass = false;
        ThesisMain thesisMain = thesisMainService.getByStudentId(studentId);
        if (thesisMain != null) {
            thesisPass = thesisProcessRecordService.isProcessPassed(thesisMain.getId(), ProcessType.FINAL_THESIS.getCode());
        }
        audit.setThesisPass(thesisPass ? 1 : 2);

        // === 3. 实践检查：查询学生已通过的创新创业项目（content_type=3, approval_status=4） ===
        long practiceCount = academicSubmissionService.count(
                new LambdaQueryWrapper<AcademicSubmission>()
                        .eq(AcademicSubmission::getStudentId, studentId)
                        .eq(AcademicSubmission::getContentType, 3) // 创新创业项目
                        .eq(AcademicSubmission::getApprovalStatus, 4) // 已通过
                        .eq(AcademicSubmission::getIsDeleted, 0) // 未删除
        );
        String requiredPracticeStr = systemConfigService.getConfigValue("graduation_required_practice");
        int requiredPractice = requiredPracticeStr != null ? Integer.parseInt(requiredPracticeStr) : 1;
        audit.setPracticePass(practiceCount >= requiredPractice ? 1 : 2);

        // === 综合判定：三项全部通过则总结果为通过 ===
        if (audit.getCreditsPass() == 1 && audit.getThesisPass() == 1 && audit.getPracticePass() == 1) {
            audit.setAuditStatus(1); // 通过
        } else {
            audit.setAuditStatus(2); // 不通过
        }

        audit.setUpdateTime(new Date());
        saveOrUpdate(audit);

        // SSE通知推送给学生
        try {
            String resultText = audit.getAuditStatus() == 1 ? "通过" : "不通过";
            String title = "毕业资格审核通知";
            String content = "您的毕业资格自动审核已完成，审核结果为：" + resultText;
            // 获取学生的用户ID用于推送通知
            Student student = studentService.getById(studentId);
            if (student != null && student.getUserId() != null) {
                noticeService.createAndPush(title, content, "1", student.getUserId());
            }
        } catch (Exception e) {
            logger.warn("毕业审核通知推送失败，不影响审核流程: {}", e.getMessage());
        }

        return audit;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchAutoAudit(Integer cohortYear, String department, String major) {
        // 按条件筛选学生
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        if (cohortYear != null) {
            studentWrapper.eq(Student::getAdmissionYear, cohortYear);
        }
        if (department != null && !department.isEmpty()) {
            studentWrapper.eq(Student::getDepartment, department);
        }
        if (major != null && !major.isEmpty()) {
            studentWrapper.eq(Student::getMajor, major);
        }
        // 只审核在读学生
        studentWrapper.eq(Student::getStatus, 1);

        List<Student> students = studentService.list(studentWrapper);

        int successCount = 0;
        int failCount = 0;
        List<String> failMessages = new ArrayList<>();

        for (Student student : students) {
            try {
                autoAudit(student.getId());
                successCount++;
            } catch (Exception e) {
                failCount++;
                failMessages.add(student.getStudentName() + "(" + student.getStudentNo() + "): " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", students.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failMessages", failMessages);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean manualAudit(Long id, Integer status, String comment, Long auditorId, String auditorName) {
        GraduationAudit audit = getById(id);
        if (audit == null) {
            throw new RuntimeException("审核记录不存在");
        }

        audit.setAuditStatus(status);
        audit.setComment(comment);
        audit.setAuditorId(auditorId);
        audit.setAuditTime(new Date());
        audit.setUpdateTime(new Date());

        boolean result = updateById(audit);

        // SSE通知推送给学生
        if (result) {
            try {
                String resultText = status == 1 ? "通过" : "不通过";
                String title = "毕业资格审核通知";
                String content = "您的毕业资格人工审核已完成，审核结果为：" + resultText;
                if (comment != null && !comment.trim().isEmpty()) {
                    content += "，审核意见：" + comment;
                }
                Student student = studentService.getById(audit.getStudentId());
                if (student != null && student.getUserId() != null) {
                    noticeService.createAndPush(title, content, "1", student.getUserId());
                }
            } catch (Exception e) {
                logger.warn("毕业审核通知推送失败，不影响审核流程: {}", e.getMessage());
            }
        }

        return result;
    }

    @Override
    public IPage<GraduationAuditWithDetailsDTO> listWithDetails(IPage<GraduationAuditWithDetailsDTO> page,
                                                                 String studentNo, String studentName,
                                                                 Integer auditStatus, Integer cohortYear,
                                                                 String department, String major) {
        return baseMapper.listWithDetails(page, studentNo, studentName, auditStatus, cohortYear, department, major);
    }

    @Override
    public GraduationAuditWithDetailsDTO getDetailWithDetails(Long id) {
        return baseMapper.getDetailWithDetails(id);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long total = count();
        long passed = count(new LambdaQueryWrapper<GraduationAudit>().eq(GraduationAudit::getAuditStatus, 1));
        long auditing = count(new LambdaQueryWrapper<GraduationAudit>().eq(GraduationAudit::getAuditStatus, 0));
        long failed = count(new LambdaQueryWrapper<GraduationAudit>().eq(GraduationAudit::getAuditStatus, 2));

        stats.put("total", total);
        stats.put("passed", passed);
        stats.put("auditing", auditing);
        stats.put("failed", failed);
        return stats;
    }

    /**
     * 计算学生已修总学分（JOIN查询，替代N+1循环）
     * 公共方法，供其他服务复用
     */
    @Override
    public BigDecimal calculateTotalCredits(Long studentId) {
        BigDecimal result = baseMapper.calculateStudentCredits(studentId);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public GraduationAudit autoAuditInNewTransaction(Long studentId) {
        return autoAudit(studentId);
    }
}
