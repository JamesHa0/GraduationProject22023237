package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ThesisExternalReviewMapper;
import com.jameshao.gp22023237.po.ThesisExternalReview;
import com.jameshao.gp22023237.service.ThesisExternalReviewService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ThesisExternalReviewServiceImpl extends ServiceImpl<ThesisExternalReviewMapper, ThesisExternalReview>
        implements ThesisExternalReviewService {

    @Override
    public boolean submitReview(ThesisExternalReview review) {
        review.setCreateTime(new Date());
        review.setUpdateTime(new Date());
        review.setReviewResult(0);
        review.setStatus(1);
        return save(review);
    }

    @Override
    public boolean recordReviewResult(Long id, Integer result, String comments) {
        ThesisExternalReview review = getById(id);
        if (review == null) return false;

        review.setReviewResult(result);
        review.setReviewComments(comments);
        review.setReviewTime(new Date());
        review.setUpdateTime(new Date());
        review.setStatus(2);

        return updateById(review);
    }
}
