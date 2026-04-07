package com.jameshao.gp22023237.DTO;

import lombok.Data;

/**
 * 成绩带课程和学生详情DTO
 */
@Data
public class ScoreWithDetailsDTO {
    /**
     * 成绩ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 学分
     */
    private Double credit;

    /**
     * 学时
     */
    private Integer hours;

    /**
     * 学期
     */
    private String semester;

    /**
     * 修读性质
     */
    private String studyNature;

    /**
     * 授课教师姓名
     */
    private String teacherName;

    /**
     * 成绩（兼容旧版本）
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
     * 平时成绩权重
     */
    private Double usualWeight;

    /**
     * 期末成绩权重
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
     * 更新时间
     */
    private String updateTime;
}
