package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.StudentStatusChange;

public interface StudentStatusChangeService extends IService<StudentStatusChange> {

    /**
     * 提交学籍异动申请
     */
    boolean submitApplication(StudentStatusChange application);

    /**
     * 导师审批
     */
    boolean mentorApprove(Long id, Integer status, String comment);

    /**
     * 教学秘书审批
     */
    boolean secretaryApprove(Long id, Integer status, String comment);

    /**
     * 分管院长审批
     */
    boolean deanApprove(Long id, Integer status, String comment);
}
