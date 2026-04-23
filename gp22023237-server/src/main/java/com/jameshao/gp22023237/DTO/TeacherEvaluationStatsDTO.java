package com.jameshao.gp22023237.DTO;

import lombok.Data;

/**
 * 教师评价统计DTO
 */
@Data
public class TeacherEvaluationStatsDTO {
    /** 教师ID */
    private Long teacherId;
    /** 教师姓名 */
    private String teacherName;
    /** 评价总数 */
    private Integer totalEvaluations;
    /** 教学态度平均分 */
    private Double avgAttitudeScore;
    /** 教学内容平均分 */
    private Double avgContentScore;
    /** 教学方法平均分 */
    private Double avgMethodScore;
    /** 教学效果平均分 */
    private Double avgEffectScore;
    /** 综合平均分 */
    private Double avgOverallScore;
}
