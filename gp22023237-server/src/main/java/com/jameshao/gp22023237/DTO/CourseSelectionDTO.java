package com.jameshao.gp22023237.DTO;

import lombok.Data;

@Data
public class CourseSelectionDTO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Integer status;

    private String courseNo;
    private String courseName;
    private Double credit;
    private Integer hours;
    private String teacherName;
    private Integer dayOfWeek;
    private String startTime;
    private String endTime;
    private String classroom;
}
