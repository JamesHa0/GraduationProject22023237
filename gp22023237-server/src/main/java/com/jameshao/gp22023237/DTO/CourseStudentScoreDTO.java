package com.jameshao.gp22023237.DTO;

import lombok.Data;

/**
 * 课程学生成绩DTO - 查询某课程下所有选课学生及成绩状态
 */
@Data
public class CourseStudentScoreDTO {
    /** 成绩ID（未录入为null） */
    private Long scoreId;
    /** 学生ID */
    private Long studentId;
    /** 学号 */
    private String studentNo;
    /** 学生姓名 */
    private String studentName;
    /** 课程ID */
    private Long courseId;
    /** 课程名称 */
    private String courseName;
    /** 平时成绩 */
    private Double usualScore;
    /** 期末成绩 */
    private Double examScore;
    /** 总成绩 */
    private Double totalScore;
    /** 平时权重 */
    private Double usualWeight;
    /** 期末权重 */
    private Double examWeight;
    /** 等级 */
    private String grade;
    /** 评语 */
    private String comment;
    /** 录入教师ID */
    private Long teacherId;
    /** 更新时间 */
    private String updateTime;
    /** 是否已完成教学评价 */
    private Boolean evaluated;
}
