package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.po.StudentStatusChange;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* 学籍异动申请Service
*/
public interface StudentStatusChangeService extends IService<StudentStatusChange> {
    // 提交休学申请
    boolean submitSuspensionApplication(StudentStatusChange application);
    
    // 导师审核
    boolean tutorApproval(Long id, Integer approval, Long tutorId);
    
    // 辅导员/教学秘书审核
    boolean counselorApproval(Long id, Integer approval, Long counselorId);
}
