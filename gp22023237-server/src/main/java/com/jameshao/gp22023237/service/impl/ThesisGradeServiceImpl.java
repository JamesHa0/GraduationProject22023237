package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ThesisGradeMapper;
import com.jameshao.gp22023237.po.ThesisGrade;
import com.jameshao.gp22023237.service.ThesisGradeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Service
public class ThesisGradeServiceImpl extends ServiceImpl<ThesisGradeMapper, ThesisGrade>
        implements ThesisGradeService {

    @Override
    public ThesisGrade getByThesisId(Long thesisId) {
        return getOne(new LambdaQueryWrapper<ThesisGrade>().eq(ThesisGrade::getThesisId, thesisId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean supervisorScore(Long thesisId, BigDecimal score, String comment) {
        ThesisGrade grade = getByThesisId(thesisId);
        if (grade == null) {
            grade = new ThesisGrade();
            grade.setThesisId(thesisId);
            grade.setSupervisorScore(score);
            grade.setSupervisorComment(comment);
            grade.setSupervisorTime(new Date());
            return save(grade);
        }
        grade.setSupervisorScore(score);
        grade.setSupervisorComment(comment);
        grade.setSupervisorTime(new Date());
        return updateById(grade);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewerScore(Long thesisId, BigDecimal score, String comment) {
        ThesisGrade grade = getByThesisId(thesisId);
        if (grade == null) {
            grade = new ThesisGrade();
            grade.setThesisId(thesisId);
            grade.setReviewerScore(score);
            grade.setReviewerComment(comment);
            grade.setReviewerTime(new Date());
            return save(grade);
        }
        grade.setReviewerScore(score);
        grade.setReviewerComment(comment);
        grade.setReviewerTime(new Date());
        return updateById(grade);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean defenseScore(Long thesisId, BigDecimal score, String comment) {
        ThesisGrade grade = getByThesisId(thesisId);
        if (grade == null) {
            grade = new ThesisGrade();
            grade.setThesisId(thesisId);
            grade.setDefenseScore(score);
            grade.setDefenseComment(comment);
            grade.setDefenseTime(new Date());
            return save(grade);
        }
        grade.setDefenseScore(score);
        grade.setDefenseComment(comment);
        grade.setDefenseTime(new Date());
        return updateById(grade);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ThesisGrade calculateTotal(Long thesisId) {
        ThesisGrade grade = getByThesisId(thesisId);
        if (grade == null) {
            throw new IllegalArgumentException("未找到成绩记录");
        }

        // 默认权重：指导教师20%、评阅30%、答辩50%
        // TODO: 后续可从ProcessConfig的scoreWeight字段读取
        BigDecimal supervisorWeight = new BigDecimal("0.20");
        BigDecimal reviewerWeight = new BigDecimal("0.30");
        BigDecimal defenseWeight = new BigDecimal("0.50");

        // 计算总评
        BigDecimal total = BigDecimal.ZERO;
        if (grade.getSupervisorScore() != null) {
            total = total.add(grade.getSupervisorScore().multiply(supervisorWeight));
        }
        if (grade.getReviewerScore() != null) {
            total = total.add(grade.getReviewerScore().multiply(reviewerWeight));
        }
        if (grade.getDefenseScore() != null) {
            total = total.add(grade.getDefenseScore().multiply(defenseWeight));
        }

        grade.setTotalScore(total.setScale(2, RoundingMode.HALF_UP));

        // 自动计算等级
        if (total.compareTo(new BigDecimal("90")) >= 0) {
            grade.setGradeLevel("优秀");
        } else if (total.compareTo(new BigDecimal("80")) >= 0) {
            grade.setGradeLevel("良好");
        } else if (total.compareTo(new BigDecimal("70")) >= 0) {
            grade.setGradeLevel("中等");
        } else if (total.compareTo(new BigDecimal("60")) >= 0) {
            grade.setGradeLevel("及格");
        } else {
            grade.setGradeLevel("不及格");
        }

        updateById(grade);
        return grade;
    }
}
