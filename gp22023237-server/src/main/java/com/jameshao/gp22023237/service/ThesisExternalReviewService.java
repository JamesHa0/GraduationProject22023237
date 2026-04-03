package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.ThesisExternalReview;

public interface ThesisExternalReviewService extends IService<ThesisExternalReview> {

    boolean submitReview(ThesisExternalReview review);

    boolean recordReviewResult(Long id, Integer result, String comments);
}
