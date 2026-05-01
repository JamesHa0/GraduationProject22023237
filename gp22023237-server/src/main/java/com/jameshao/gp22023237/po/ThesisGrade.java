package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 成绩评定表
 * @TableName thesis_grade
 */
@TableName(value = "thesis_grade")
@Data
public class ThesisGrade {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联论文主ID */
    private Long thesisId;

    /** 指导教师评分 */
    private BigDecimal supervisorScore;

    /** 指导教师评语 */
    private String supervisorComment;

    /** 指导教师评分时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date supervisorTime;

    /** 评阅教师评分 */
    private BigDecimal reviewerScore;

    /** 评阅教师评语 */
    private String reviewerComment;

    /** 评阅评分时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewerTime;

    /** 答辩评分 */
    private BigDecimal defenseScore;

    /** 答辩评语 */
    private String defenseComment;

    /** 答辩评分时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date defenseTime;

    /** 总评成绩 */
    private BigDecimal totalScore;

    /** 等级：优秀/良好/中等/及格/不及格 */
    private String gradeLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
