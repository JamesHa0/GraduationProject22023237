package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.MentorChangeApplicationMapper;
import com.jameshao.gp22023237.po.MentorChangeApplication;
import com.jameshao.gp22023237.po.MentorStudent;
import com.jameshao.gp22023237.service.MentorChangeApplicationService;
import com.jameshao.gp22023237.service.MentorStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class MentorChangeApplicationServiceImpl extends ServiceImpl<MentorChangeApplicationMapper, MentorChangeApplication>
        implements MentorChangeApplicationService {

    @Autowired
    private MentorStudentService mentorStudentService;

    @Override
    @Transactional
    public boolean submitApplication(MentorChangeApplication application) {
        // 设置申请时间
        Date now = new Date();
        application.setApplyTime(now);
        application.setCreateTime(now);
        application.setUpdateTime(now);

        // 初始状态
        application.setOriginalMentorStatus(0);
        application.setNewMentorStatus(0);
        application.setOverallStatus(0);

        return save(application);
    }

    @Override
    @Transactional
    public boolean originalMentorApprove(Long id, Integer status, String comment) {
        MentorChangeApplication application = getById(id);
        if (application == null) {
            return false;
        }

        Date now = new Date();
        application.setOriginalMentorStatus(status);
        application.setOriginalMentorComment(comment);
        application.setOriginalMentorTime(now);
        application.setUpdateTime(now);

        if (status == 1) {
            // 原导师通过，进入新导师审批阶段
            application.setOverallStatus(1);
        } else if (status == 2) {
            // 原导师拒绝，流程结束
            application.setOverallStatus(3);
        }

        return updateById(application);
    }

    @Override
    @Transactional
    public boolean newMentorApprove(Long id, Integer status, String comment) {
        MentorChangeApplication application = getById(id);
        if (application == null) {
            return false;
        }

        Date now = new Date();
        application.setNewMentorStatus(status);
        application.setNewMentorComment(comment);
        application.setNewMentorTime(now);
        application.setUpdateTime(now);

        if (status == 1) {
            // 新导师通过，审批完成，更新导师学生关系
            application.setOverallStatus(2);
            updateMentorStudentRelationship(application);
        } else if (status == 2) {
            // 新导师拒绝，流程结束
            application.setOverallStatus(3);
        }

        return updateById(application);
    }

    private void updateMentorStudentRelationship(MentorChangeApplication application) {
        // 1. 删除学生与原导师的关系
        LambdaQueryWrapper<MentorStudent> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(MentorStudent::getStudentId, application.getStudentId())
                .eq(MentorStudent::getMentorId, application.getOriginalMentorId());
        mentorStudentService.remove(deleteWrapper);

        // 2. 添加学生与新导师的关系
        MentorStudent newRelationship = new MentorStudent();
        newRelationship.setStudentId(application.getStudentId());
        newRelationship.setMentorId(application.getNewMentorId());
        newRelationship.setMentorType(1); // 第一导师
        newRelationship.setCreateTime(new Date());
        newRelationship.setUpdateTime(new Date());
        mentorStudentService.save(newRelationship);
    }
}
