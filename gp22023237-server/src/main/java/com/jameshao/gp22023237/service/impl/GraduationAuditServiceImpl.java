package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.GraduationAuditMapper;
import com.jameshao.gp22023237.po.GraduationAudit;
import com.jameshao.gp22023237.service.GraduationAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            audit.setCreateTime(new Date());
            audit.setUpdateTime(new Date());
        }

        // 默认设置为待审核状态
        audit.setAuditStatus(0);
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
        audit.setComment(comment);
        audit.setAuditorId(auditorId);
        audit.setAuditTime(new Date());
        audit.setUpdateTime(new Date());

        return updateById(audit);
    }
}
