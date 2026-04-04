package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.common.enums.ApprovalStatus;
import com.jameshao.gp22023237.common.enums.OverallStatus;
import com.jameshao.gp22023237.mapper.ThesisProgressMapper;
import com.jameshao.gp22023237.po.ThesisProgress;
import com.jameshao.gp22023237.service.ThesisProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ThesisProgressServiceImpl extends ServiceImpl<ThesisProgressMapper, ThesisProgress>
        implements ThesisProgressService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitProgress(ThesisProgress progress) {
        // 检查重复提交：同一学生同一类型只能有一条记录
        if (progress.getStudentId() != null && progress.getProgressType() != null) {
            LambdaQueryWrapper<ThesisProgress> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ThesisProgress::getStudentId, progress.getStudentId());
            wrapper.eq(ThesisProgress::getProgressType, progress.getProgressType());
            long count = count(wrapper);
            if (count > 0) {
                throw new IllegalStateException("该学生已提交过此类型的论文进展");
            }
        }

        progress.setSubmitTime(new Date());
        // createTime和updateTime由数据库自动维护
        progress.setMentorStatus(ApprovalStatus.UNAPPROVED.getCode());
        progress.setSecretaryStatus(ApprovalStatus.UNAPPROVED.getCode());
        progress.setDeanStatus(ApprovalStatus.UNAPPROVED.getCode());
        progress.setOverallStatus(OverallStatus.MENTOR_APPROVING.getCode());
        return save(progress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean mentorApprove(Long id, Integer status, String comment) {
        ThesisProgress progress = getById(id);
        if (progress == null) {
            throw new IllegalArgumentException("未找到记录");
        }

        // 校验当前状态是否允许导师审批
        OverallStatus currentStatus = OverallStatus.fromCode(progress.getOverallStatus());
        if (currentStatus != OverallStatus.MENTOR_APPROVING) {
            throw new IllegalStateException("当前状态不允许导师审批");
        }

        // 校验导师审批状态
        if (progress.getMentorStatus() != null && progress.getMentorStatus() != ApprovalStatus.UNAPPROVED.getCode()) {
            throw new IllegalStateException("该记录已由导师审批过");
        }

        progress.setMentorStatus(status);
        progress.setMentorComment(comment);
        progress.setMentorTime(new Date());

        if (ApprovalStatus.APPROVED.getCode().equals(status)) {
            progress.setOverallStatus(OverallStatus.SECRETARY_APPROVING.getCode());
        } else if (ApprovalStatus.REJECTED.getCode().equals(status)) {
            progress.setOverallStatus(OverallStatus.REJECTED.getCode());
        }

        return updateById(progress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean secretaryApprove(Long id, Integer status, String comment) {
        ThesisProgress progress = getById(id);
        if (progress == null) {
            throw new IllegalArgumentException("未找到记录");
        }

        // 校验当前状态是否允许秘书审批
        OverallStatus currentStatus = OverallStatus.fromCode(progress.getOverallStatus());
        if (currentStatus != OverallStatus.SECRETARY_APPROVING) {
            throw new IllegalStateException("当前状态不允许秘书审批");
        }

        // 校验秘书审批状态
        if (progress.getSecretaryStatus() != null && progress.getSecretaryStatus() != ApprovalStatus.UNAPPROVED.getCode()) {
            throw new IllegalStateException("该记录已由秘书审批过");
        }

        // 校验导师是否已通过
        if (!ApprovalStatus.APPROVED.getCode().equals(progress.getMentorStatus())) {
            throw new IllegalStateException("导师尚未通过，不能进行秘书审批");
        }

        progress.setSecretaryStatus(status);
        progress.setSecretaryComment(comment);
        progress.setSecretaryTime(new Date());

        if (ApprovalStatus.APPROVED.getCode().equals(status)) {
            progress.setOverallStatus(OverallStatus.DEAN_APPROVING.getCode());
        } else if (ApprovalStatus.REJECTED.getCode().equals(status)) {
            progress.setOverallStatus(OverallStatus.REJECTED.getCode());
        }

        return updateById(progress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deanApprove(Long id, Integer status, String comment) {
        ThesisProgress progress = getById(id);
        if (progress == null) {
            throw new IllegalArgumentException("未找到记录");
        }

        // 校验当前状态是否允许院长审批
        OverallStatus currentStatus = OverallStatus.fromCode(progress.getOverallStatus());
        if (currentStatus != OverallStatus.DEAN_APPROVING) {
            throw new IllegalStateException("当前状态不允许院长审批");
        }

        // 校验院长审批状态
        if (progress.getDeanStatus() != null && progress.getDeanStatus() != ApprovalStatus.UNAPPROVED.getCode()) {
            throw new IllegalStateException("该记录已由院长审批过");
        }

        // 校验导师和秘书是否已通过
        if (!ApprovalStatus.APPROVED.getCode().equals(progress.getMentorStatus())) {
            throw new IllegalStateException("导师尚未通过，不能进行院长审批");
        }
        if (!ApprovalStatus.APPROVED.getCode().equals(progress.getSecretaryStatus())) {
            throw new IllegalStateException("秘书尚未通过，不能进行院长审批");
        }

        progress.setDeanStatus(status);
        progress.setDeanComment(comment);
        progress.setDeanTime(new Date());

        if (ApprovalStatus.APPROVED.getCode().equals(status)) {
            progress.setOverallStatus(OverallStatus.PASSED.getCode());
        } else if (ApprovalStatus.REJECTED.getCode().equals(status)) {
            progress.setOverallStatus(OverallStatus.REJECTED.getCode());
        }

        return updateById(progress);
    }
}
