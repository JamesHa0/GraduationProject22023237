package com.jameshao.gp22023237.DTO;

import lombok.Data;

/**
 * 待评/已评课程DTO
 */
@Data
public class EvaluationCourseDTO {
    /** 课程ID */
    private Long courseId;
    /** 课程名称 */
    private String courseName;
    /** 课程编号 */
    private String courseNo;
    /** 学分 */
    private Double credit;
    /** 学时 */
    private Integer hours;
    /** 学期 */
    private String semester;
    /** 授课教师ID */
    private Long teacherId;
    /** 授课教师姓名 */
    private String teacherName;
    /** 是否已评价 */
    private Boolean evaluated;
    /** 教学态度评分 */
    private Integer attitudeScore;
    /** 教学内容评分 */
    private Integer contentScore;
    /** 教学方法评分 */
    private Integer methodScore;
    /** 教学效果评分 */
    private Integer effectScore;
    /** 综合评分(四项平均) */
    private Double overallScore;
    /** 评价时间 */
    private String evaluateTime;
    /** 文字评语 */
    private String comment;
}
