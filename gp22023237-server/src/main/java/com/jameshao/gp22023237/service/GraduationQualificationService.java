package com.jameshao.gp22023237.service;

import com.jameshao.gp22023237.po.GraduationQualification;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* 毕业资格审核Service
*/
public interface GraduationQualificationService extends IService<GraduationQualification> {
    // 自动审核毕业资格
    boolean autoReviewGraduation(Long studentId, Long reviewerId);
    
    // 手动审核毕业资格
    boolean manualReview(Long id, Integer creditCheck, Integer thesisCheck, Integer practiceCheck, String comment, Long reviewerId);
}
