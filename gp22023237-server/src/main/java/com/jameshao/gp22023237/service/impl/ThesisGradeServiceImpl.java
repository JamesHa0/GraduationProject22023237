package com.jameshao.gp22023237.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jameshao.gp22023237.mapper.ThesisGradeMapper;
import com.jameshao.gp22023237.po.ProcessConfig;
import com.jameshao.gp22023237.po.ThesisGrade;
import com.jameshao.gp22023237.service.NoticeService;
import com.jameshao.gp22023237.service.ProcessConfigService;
import com.jameshao.gp22023237.service.StudentService;
import com.jameshao.gp22023237.service.TeacherService;
import com.jameshao.gp22023237.service.ThesisGradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Service
public class ThesisGradeServiceImpl extends ServiceImpl<ThesisGradeMapper, ThesisGrade>
        implements ThesisGradeService {

    /** 分数最小值 */
    private static final BigDecimal SCORE_MIN = BigDecimal.ZERO;
    /** 分数最大值 */
    private static final BigDecimal SCORE_MAX = new BigDecimal("100");

    /** 默认权重：指导教师20%、评阅30%、答辩50% */
    private static final BigDecimal DEFAULT_SUPERVISOR_WEIGHT = new BigDecimal("0.20");
    private static final BigDecimal DEFAULT_REVIEWER_WEIGHT = new BigDecimal("0.30");
    private static final BigDecimal DEFAULT_DEFENSE_WEIGHT = new BigDecimal("0.50");

    @Autowired
    private ProcessConfigService processConfigService;

    private static final Logger logger = LoggerFactory.getLogger(ThesisGradeServiceImpl.class);

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private com.jameshao.gp22023237.service.ThesisMainService thesisMainService;

    @Override
    public ThesisGrade getByThesisId(Long thesisId) {
        return getOne(new LambdaQueryWrapper<ThesisGrade>().eq(ThesisGrade::getThesisId, thesisId));
    }

    /**
     * 校验分数范围（0-100）
     */
    private void validateScore(BigDecimal score) {
        if (score != null && (score.compareTo(SCORE_MIN) < 0 || score.compareTo(SCORE_MAX) > 0)) {
            throw new IllegalArgumentException("分数必须在0-100之间，当前值: " + score);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean supervisorScore(Long thesisId, BigDecimal score, String comment) {
        validateScore(score);
        ThesisGrade grade = getByThesisId(thesisId);
        if (grade == null) {
            grade = new ThesisGrade();
            grade.setThesisId(thesisId);
            grade.setSupervisorScore(score);
            grade.setSupervisorComment(comment);
            grade.setSupervisorTime(new Date());
            return save(grade);
        }
        // 防覆盖校验：如果总评已计算完成，不允许再修改单项评分
        if (grade.getTotalScore() != null) {
            throw new IllegalStateException("总评已计算完成，不允许再修改单项评分，如需修改请先清除总评");
        }
        grade.setSupervisorScore(score);
        grade.setSupervisorComment(comment);
        grade.setSupervisorTime(new Date());
        boolean result = updateById(grade);
        // 2.12 通知：指导教师评分 → 通知学生
        if (result) {
            try {
                com.jameshao.gp22023237.po.ThesisMain thesisMain = thesisMainService.getById(thesisId);
                if (thesisMain != null && thesisMain.getStudentId() != null) {
                    Long studentUserId = noticeService.getStudentUserId(thesisMain.getStudentId());
                    if (studentUserId != null) {
                        noticeService.createAndPush("论文评分通知",
                            "指导教师已评分：" + score + "分", "1", studentUserId);
                    }
                }
            } catch (Exception e) {
                logger.warn("指导教师评分通知推送失败: {}", e.getMessage());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewerScore(Long thesisId, BigDecimal score, String comment) {
        validateScore(score);
        ThesisGrade grade = getByThesisId(thesisId);
        if (grade == null) {
            grade = new ThesisGrade();
            grade.setThesisId(thesisId);
            grade.setReviewerScore(score);
            grade.setReviewerComment(comment);
            grade.setReviewerTime(new Date());
            return save(grade);
        }
        // 防覆盖校验
        if (grade.getTotalScore() != null) {
            throw new IllegalStateException("总评已计算完成，不允许再修改单项评分，如需修改请先清除总评");
        }
        grade.setReviewerScore(score);
        grade.setReviewerComment(comment);
        grade.setReviewerTime(new Date());
        boolean result = updateById(grade);
        // 2.13 通知：评阅教师评分 → 通知学生
        if (result) {
            try {
                com.jameshao.gp22023237.po.ThesisMain thesisMain = thesisMainService.getById(thesisId);
                if (thesisMain != null && thesisMain.getStudentId() != null) {
                    Long studentUserId = noticeService.getStudentUserId(thesisMain.getStudentId());
                    if (studentUserId != null) {
                        noticeService.createAndPush("论文评分通知",
                            "评阅教师已评分：" + score + "分", "1", studentUserId);
                    }
                }
            } catch (Exception e) {
                logger.warn("评阅教师评分通知推送失败: {}", e.getMessage());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean defenseScore(Long thesisId, BigDecimal score, String comment) {
        validateScore(score);
        ThesisGrade grade = getByThesisId(thesisId);
        if (grade == null) {
            grade = new ThesisGrade();
            grade.setThesisId(thesisId);
            grade.setDefenseScore(score);
            grade.setDefenseComment(comment);
            grade.setDefenseTime(new Date());
            return save(grade);
        }
        // 防覆盖校验
        if (grade.getTotalScore() != null) {
            throw new IllegalStateException("总评已计算完成，不允许再修改单项评分，如需修改请先清除总评");
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

        // 校验三方评分必须全部非null
        if (grade.getSupervisorScore() == null || grade.getReviewerScore() == null || grade.getDefenseScore() == null) {
            throw new IllegalStateException("三方评分必须全部录入后才能计算总评（指导教师、评阅、答辩）");
        }

        // 从ProcessConfig读取权重配置，未配置则使用默认值
        // 按processType=7(毕业论文)的scoreWeight作为总权重配置参考
        // scoreWeight存储格式示例：{"supervisor":0.20,"reviewer":0.30,"defense":0.50}
        BigDecimal supervisorWeight = DEFAULT_SUPERVISOR_WEIGHT;
        BigDecimal reviewerWeight = DEFAULT_REVIEWER_WEIGHT;
        BigDecimal defenseWeight = DEFAULT_DEFENSE_WEIGHT;

        // 尝试从毕业论文环节(processType=7)读取权重配置
        ProcessConfig config = processConfigService.getOne(
            new LambdaQueryWrapper<ProcessConfig>().eq(ProcessConfig::getProcessType, 7));
        if (config != null && config.getScoreWeight() != null) {
            // scoreWeight字段存储的是答辩权重比例，其他权重按比例分配
            // 如果scoreWeight是单个数值，视为答辩权重，其余按2:3:(7-x)分配
            // 简单方案：直接使用配置的scoreWeight作为答辩权重
            defenseWeight = config.getScoreWeight();
            // 剩余权重按默认比例 2:3 分配给导师和评阅
            BigDecimal remaining = BigDecimal.ONE.subtract(defenseWeight);
            supervisorWeight = remaining.multiply(new BigDecimal("0.4")).setScale(2, RoundingMode.HALF_UP);
            reviewerWeight = remaining.subtract(supervisorWeight).setScale(2, RoundingMode.HALF_UP);
        }

        // 计算总评
        BigDecimal total = BigDecimal.ZERO;
        total = total.add(grade.getSupervisorScore().multiply(supervisorWeight));
        total = total.add(grade.getReviewerScore().multiply(reviewerWeight));
        total = total.add(grade.getDefenseScore().multiply(defenseWeight));

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
        // 2.11 通知：论文总评计算 → 通知学生
        try {
            com.jameshao.gp22023237.po.ThesisMain thesisMain = thesisMainService.getById(thesisId);
            if (thesisMain != null && thesisMain.getStudentId() != null) {
                Long studentUserId = noticeService.getStudentUserId(thesisMain.getStudentId());
                if (studentUserId != null) {
                    noticeService.createAndPush("论文总评成绩通知",
                        "论文总评成绩：" + grade.getTotalScore() + "分（" + grade.getGradeLevel() + "）",
                        "1", studentUserId);
                }
            }
        } catch (Exception e) {
            logger.warn("论文总评通知推送失败: {}", e.getMessage());
        }
        return grade;
    }
}
