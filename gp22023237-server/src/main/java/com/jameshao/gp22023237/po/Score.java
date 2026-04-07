package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 成绩表
 * @TableName score
 */
@TableName(value ="score")
@Data
public class Score {
    /**
     * 成绩ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 成绩（兼容旧版本，建议使用total_score）
     */
    private Double score;

    /**
     * 平时成绩
     */
    private Double usualScore;

    /**
     * 期末成绩
     */
    private Double examScore;

    /**
     * 总成绩
     */
    private Double totalScore;

    /**
     * 平时成绩权重(默认30%)
     */
    private Double usualWeight;

    /**
     * 期末成绩权重(默认70%)
     */
    private Double examWeight;

    /**
     * 等级：A-优秀，B-良好，C-中等，D-及格，E-不及格
     */
    private String grade;

    /**
     * 评语
     */
    private String comment;

    /**
     * 录入教师ID
     */
    private Long teacherId;

    /**
     * 更新时间
     */
    private Date updateTime;
}
