package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.AcademicAchievement;

public interface AcademicAchievementService extends IService<AcademicAchievement> {

    boolean submitAchievement(AcademicAchievement achievement);

    boolean mentorApprove(Long id, Integer status, String comment);

    boolean secretaryApprove(Long id, Integer status, String comment);

    boolean deanApprove(Long id, Integer status, String comment);

    boolean removeById(Long id);
}
