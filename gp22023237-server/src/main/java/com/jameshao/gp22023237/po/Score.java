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
     * 成绩
     */
    private Double score;

    /**
     * 等级：优秀，良好，中等，及格，不及格
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
