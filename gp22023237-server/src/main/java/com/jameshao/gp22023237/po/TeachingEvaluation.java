package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 教学评价表
 * @TableName teaching_evaluation
 */
@TableName(value ="teaching_evaluation")
@Data
public class TeachingEvaluation {
    /**
     * 评价ID
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
     * 被评价教师ID
     */
    private Long teacherId;

    /**
     * 教学态度评分(1-5)
     */
    private Integer attitudeScore;

    /**
     * 教学内容评分(1-5)
     */
    private Integer contentScore;

    /**
     * 教学方法评分(1-5)
     */
    private Integer methodScore;

    /**
     * 教学效果评分(1-5)
     */
    private Integer effectScore;

    /**
     * 文字评语
     */
    private String comment;

    /**
     * 评价时间
     */
    private Date createTime;
}
