package com.jameshao.gp22023237.DTO;

import lombok.Data;

/**
 * 提交教学评价DTO
 */
@Data
public class TeachingEvaluationDTO {
    /** 课程ID */
    private Long courseId;
    /** 教学态度评分(1-5) */
    private Integer attitudeScore;
    /** 教学内容评分(1-5) */
    private Integer contentScore;
    /** 教学方法评分(1-5) */
    private Integer methodScore;
    /** 教学效果评分(1-5) */
    private Integer effectScore;
    /** 文字评语 */
    private String comment;
}
