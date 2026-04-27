package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.util.Date;

/**
 * 导师更换申请带详情DTO
 */
@Data
public class MentorChangeApplicationWithDetailsDTO {
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String studentDepartment;
    private String major;
    private Long originalMentorId;
    private String originalMentorNo;
    private String originalMentorName;
    private String originalMentorTitle;
    private String originalMentorDepartment;
    private Long newMentorId;
    private String newMentorNo;
    private String newMentorName;
    private String newMentorTitle;
    private String newMentorDepartment;
    private String changeReason;
    private Integer originalMentorStatus;
    private String originalMentorComment;
    private Date originalMentorTime;
    private Integer newMentorStatus;
    private String newMentorComment;
    private Date newMentorTime;
    private Integer overallStatus;
    private Date applyTime;
    private Date createTime;
    private Date updateTime;
}
