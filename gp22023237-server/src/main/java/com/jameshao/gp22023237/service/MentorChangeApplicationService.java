package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.MentorChangeApplication;

public interface MentorChangeApplicationService extends IService<MentorChangeApplication> {

    /**
     * 提交导师更换申请
     */
    boolean submitApplication(MentorChangeApplication application);

    /**
     * 原导师审批
     */
    boolean originalMentorApprove(Long id, Integer status, String comment);

    /**
     * 新导师审批
     */
    boolean newMentorApprove(Long id, Integer status, String comment);
}
