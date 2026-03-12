package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.GraduationQualificationMapper;
import com.jameshao.gp22023237.mapper.ScoreMapper;
import com.jameshao.gp22023237.service.GraduationQualificationService;
import com.jameshao.gp22023237.po.GraduationQualification;
import com.jameshao.gp22023237.po.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 毕业资格审核Service实现
 */
@Service
public class GraduationQualificationServiceImpl extends ServiceImpl<GraduationQualificationMapper, GraduationQualification>
        implements GraduationQualificationService {

    @Autowired
    private ScoreMapper scoreMapper;

    private static final int REQUIRED_CREDITS = 35; // 要求学分

    @Override
    @Transactional
    public boolean autoReviewGraduation(Long studentId, Long reviewerId) {
        // 检查是否已存在审核记录
        GraduationQualification qualification = getOne(new QueryWrapper<GraduationQualification>()
                .eq("student_id", studentId));
        if (qualification == null) {
            qualification = new GraduationQualification();
            qualification.setStudentId(studentId);
        }

        // 1. 检查学分
        List<Score> scores = scoreMapper.selectList(new QueryWrapper<Score>()
                .eq("student_id", studentId)
                .ge("score", 60)); // 及格以上
        double totalCredits = scores.stream().mapToDouble(Score::getCredit).sum();
        qualification.setCreditCheck(totalCredits >= REQUIRED_CREDITS ? 1 : 0);

        // 2. 检查论文答辩（暂时通过，待论文答辩模块完善）
        qualification.setThesisCheck(1);

        // 3. 检查实践环节（暂时通过）
        qualification.setPracticeCheck(1);

        // 4. 综合结果
        qualification.setOverallResult(
                (qualification.getCreditCheck() == 1 &&
                        qualification.getThesisCheck() == 1 &&
                        qualification.getPracticeCheck() == 1) ? 1 : 0);

        qualification.setReviewerId(reviewerId);
        qualification.setReviewTime(new Date());
        qualification.setUpdateTime(new Date());

        return saveOrUpdate(qualification);
    }

    @Override
    @Transactional
    public boolean manualReview(Long id, Integer creditCheck, Integer thesisCheck,
                                Integer practiceCheck, String comment, Long reviewerId) {
        GraduationQualification qualification = getById(id);
        if (qualification == null) {
            return false;
        }

        qualification.setCreditCheck(creditCheck);
        qualification.setThesisCheck(thesisCheck);
        qualification.setPracticeCheck(practiceCheck);
        qualification.setComment(comment);
        qualification.setReviewerId(reviewerId);
        qualification.setReviewTime(new Date());

        // 综合结果
        qualification.setOverallResult(
                (creditCheck == 1 && thesisCheck == 1 && practiceCheck == 1) ? 1 : 0);

        qualification.setUpdateTime(new Date());
        return updateById(qualification);
    }
}
