package com.jameshao.gp22023237.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jameshao.gp22023237.po.ThesisGrade;

public interface ThesisGradeService extends IService<ThesisGrade> {

    /** 获取某论文的成绩评定 */
    ThesisGrade getByThesisId(Long thesisId);

    /** 指导教师评分 */
    boolean supervisorScore(Long thesisId, java.math.BigDecimal score, String comment);

    /** 评阅教师评分 */
    boolean reviewerScore(Long thesisId, java.math.BigDecimal score, String comment);

    /** 答辩评分 */
    boolean defenseScore(Long thesisId, java.math.BigDecimal score, String comment);

    /** 计算总评成绩 */
    ThesisGrade calculateTotal(Long thesisId);
}
