package com.jameshao.gp22023237.DTO;

import lombok.Data;

/**
 * 班级DTO
 */
@Data
public class ClassDTO {
    private Long id;
    private String className;
    private String department;
    private String major;
    private Integer admissionYear;
    private Integer studentCount;
    private String createTime;
    private String updateTime;
}
