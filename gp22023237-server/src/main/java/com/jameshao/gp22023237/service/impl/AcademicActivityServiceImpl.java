package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.AcademicActivityMapper;
import com.jameshao.gp22023237.po.AcademicActivity;
import com.jameshao.gp22023237.service.AcademicActivityService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AcademicActivityServiceImpl extends ServiceImpl<AcademicActivityMapper, AcademicActivity>
        implements AcademicActivityService {

    @Override
    public boolean submitActivity(AcademicActivity activity) {
        activity.setSubmitTime(new Date());
        activity.setCreateTime(new Date());
        activity.setUpdateTime(new Date());
        activity.setMentorStatus(0);
        activity.setSecretaryStatus(0);
        activity.setDeanStatus(0);
        return save(activity);
    }

    @Override
    public boolean mentorApprove(Long id, Integer status, String comment) {
        AcademicActivity activity = getById(id);
        if (activity == null) return false;
        activity.setMentorStatus(status);
        activity.setMentorComment(comment);
        activity.setUpdateTime(new Date());
        return updateById(activity);
    }

    @Override
    public boolean secretaryApprove(Long id, Integer status, String comment) {
        AcademicActivity activity = getById(id);
        if (activity == null) return false;
        activity.setSecretaryStatus(status);
        activity.setSecretaryComment(comment);
        activity.setUpdateTime(new Date());
        return updateById(activity);
    }

    @Override
    public boolean deanApprove(Long id, Integer status, String comment) {
        AcademicActivity activity = getById(id);
        if (activity == null) return false;
        activity.setDeanStatus(status);
        activity.setDeanComment(comment);
        activity.setUpdateTime(new Date());
        return updateById(activity);
    }
}
