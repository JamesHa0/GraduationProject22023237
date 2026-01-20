package com.jameshao.gp22023237.DTO;

import lombok.Data;

@Data
public class SelectionDTO {
    private Long id;// 主键id
    private Integer round; // 轮次
    private Long studentId; // 学生id
    private Long mentorId; // 导师id
    private String studentNo; // 学生学号
    private String department; // 学生院系
    private String major; // 学生专业
    private Object admissionYear; // 学生入学年份
    private Integer studentChoiceOrder; // 学生志愿顺序
    private String studentName; // 学生姓名
    private String mentorName; // 导师姓名
    private Long userId; // 学生的用户id
    private Integer teacherStatus; // 导师选择状态

}
