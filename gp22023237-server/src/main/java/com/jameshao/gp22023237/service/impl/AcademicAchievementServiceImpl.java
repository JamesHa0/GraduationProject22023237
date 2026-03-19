package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.AcademicAchievementMapper;
import com.jameshao.gp22023237.po.AcademicAchievement;
import com.jameshao.gp22023237.service.AcademicAchievementService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AcademicAchievementServiceImpl extends ServiceImpl<AcademicAchievementMapper, AcademicAchievement>
        implements AcademicAchievementService {

    @Override
    public boolean submitAchievement(AcademicAchievement achievement) {
        achievement.setSubmitTime(new Date());
        achievement.setCreateTime(new Date());
        achievement.setUpdateTime(new Date());
        achievement.setMentorStatus(0);
        achievement.setSecretaryStatus(0);
        achievement.setDeanStatus(0);
        return save(achievement);
    }

    @Override
    public boolean mentorApprove(Long id, Integer status, String comment) {
        AcademicAchievement achievement = getById(id);
        if (achievement == null) return false;
        achievement.setMentorStatus(status);
        achievement.setMentorComment(comment);
        achievement.setUpdateTime(new Date());
        return updateById(achievement);
    }

    @Override
    public boolean secretaryApprove(Long id, Integer status, String comment) {
        AcademicAchievement achievement = getById(id);
        if (achievement == null) return false;
        achievement.setSecretaryStatus(status);
        achievement.setSecretaryComment(comment);
        achievement.setUpdateTime(new Date());
        return updateById(achievement);
    }

    @Override
    public boolean deanApprove(Long id, Integer status, String comment) {
        AcademicAchievement achievement = getById(id);
        if (achievement == null) return false;
        achievement.setDeanStatus(status);
        achievement.setDeanComment(comment);
        achievement.setUpdateTime(new Date());
        return updateById(achievement);
    }

    @Override
    public boolean removeById(Long id) {
        return removeById(id);
    }
}
