package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.common.enums.ApprovalStatus;
import com.jameshao.gp22023237.mapper.DegreeApplicationMapper;
import com.jameshao.gp22023237.po.DegreeApplication;
import com.jameshao.gp22023237.service.DegreeApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class DegreeApplicationServiceImpl extends ServiceImpl<DegreeApplicationMapper, DegreeApplication>
        implements DegreeApplicationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitApplication(DegreeApplication application) {
        // 检查重复提交：同一学生只能有一条学位申请记录
        if (application.getStudentId() != null) {
            LambdaQueryWrapper<DegreeApplication> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DegreeApplication::getStudentId, application.getStudentId());
            long count = count(wrapper);
            if (count > 0) {
                throw new IllegalStateException("该学生已提交过学位申请");
            }
        }

        application.setSubmitTime(new Date());
        // createTime和updateTime由数据库自动维护
        application.setCommitteeStatus(ApprovalStatus.UNAPPROVED.getCode());
        application.setDegreeGranted(0);
        return save(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean committeeApprove(Long id, Integer status, String comment) {
        DegreeApplication application = getById(id);
        if (application == null) {
            throw new IllegalArgumentException("未找到记录");
        }

        // 校验当前分委会状态是否允许审批
        if (application.getCommitteeStatus() != null &&
            application.getCommitteeStatus() != ApprovalStatus.UNAPPROVED.getCode()) {
            throw new IllegalStateException("该申请已由分委会审批过");
        }

        application.setCommitteeStatus(status);
        application.setCommitteeComment(comment);

        return updateById(application);
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

        // 检查答辩结果是否通过（defenseResult: 1=通过）
        if (application.getDefenseResult() == null || application.getDefenseResult() != 1) {
            throw new IllegalStateException("答辩未通过，不能授予学位");
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

        return updateById(application);
    }
}
