package com.jameshao.gp22023237.DTO;

import lombok.Data;

@Data
public class SelectionDTO {
    private Integer round;
    private Long studentId;
    private Long mentorId;
    private String studentNo;
    private String department;
    private String major;
    private Object admissionYear;
    private Integer studentChoiceOrder;
    private String studentName;
    private String mentorName;

}
