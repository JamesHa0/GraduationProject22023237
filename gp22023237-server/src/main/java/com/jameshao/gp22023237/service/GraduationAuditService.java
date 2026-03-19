package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.GraduationAudit;

public interface GraduationAuditService extends IService<GraduationAudit> {

    /**
     * 自动审核毕业资格
     */
    GraduationAudit autoAudit(Long studentId);

    /**
     * 人工审核毕业资格
     */
    boolean manualAudit(Long id, Integer status, String comment, Long auditorId, String auditorName);
}
