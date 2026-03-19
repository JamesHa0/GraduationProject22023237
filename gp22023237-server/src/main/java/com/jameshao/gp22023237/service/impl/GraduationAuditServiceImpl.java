package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.GraduationAuditMapper;
import com.jameshao.gp22023237.po.GraduationAudit;
import com.jameshao.gp22023237.service.GraduationAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class GraduationAuditServiceImpl extends ServiceImpl<GraduationAuditMapper, GraduationAudit>
        implements GraduationAuditService {

    @Override
    @Transactional
    public GraduationAudit autoAudit(Long studentId) {
        // 查询或创建审核记录
        GraduationAudit audit = lambdaQuery()
                .eq(GraduationAudit::getStudentId, studentId)
                .one();

        if (audit == null) {
            audit = new GraduationAudit();
            audit.setStudentId(studentId);
            audit.setApplyTime(new Date());
            audit.setCreateTime(new Date());
            audit.setUpdateTime(new Date());
        }

        // 判断学分是否达标
        audit.setCreditsPass(audit.getTotalCredits() != null &&
                audit.getTotalCredits().compareTo(audit.getRequiredCredits() != null ?
                        audit.getRequiredCredits() : BigDecimal.ZERO) >= 0 ? 1 : 0);

        // 判断综合审核状态
        boolean allPass = audit.getCreditsPass() == 1 &&
                (audit.getThesisProposalPass() == null ? 0 : audit.getThesisProposalPass()) == 1 &&
                (audit.getMidtermPass() == null ? 0 : audit.getMidtermPass()) == 1 &&
                (audit.getPreDefensePass() == null ? 0 : audit.getPreDefensePass()) == 1 &&
                (audit.getDefensePass() == null ? 0 : audit.getDefensePass()) == 1 &&
                (audit.getThesisPass() == null ? 0 : audit.getThesisPass()) == 1 &&
                (audit.getAcademicPass() == null ? 0 : audit.getAcademicPass()) == 1;

        audit.setAuditStatus(allPass ? 1 : 0);
        audit.setUpdateTime(new Date());

        saveOrUpdate(audit);
        return audit;
    }

    @Override
    @Transactional
    public boolean manualAudit(Long id, Integer status, String comment, Long auditorId, String auditorName) {
        GraduationAudit audit = getById(id);
        if (audit == null) {
            return false;
        }

        audit.setAuditStatus(status);
        audit.setAuditComment(comment);
        audit.setAuditorId(auditorId);
        audit.setAuditorName(auditorName);
        audit.setAuditTime(new Date());
        audit.setUpdateTime(new Date());

        return updateById(audit);
    }
}
