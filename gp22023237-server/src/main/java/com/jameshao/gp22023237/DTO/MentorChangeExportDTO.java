package com.jameshao.gp22023237.DTO;

import lombok.Data;

/**
 * 导师更换申请表导出DTO
 */
@Data
public class MentorChangeExportDTO {
    private String studentNo;          // 学号
    private String studentName;        // 学生姓名
    private String studentDepartment;  // 学生院系
    private String major;              // 专业

    private String originalMentorName;     // 原导师姓名
    private String originalMentorTitle;    // 原导师职称
    private String originalMentorDepartment; // 原导师院系

    private String newMentorName;          // 新导师姓名
    private String newMentorTitle;         // 新导师职称
    private String newMentorDepartment;    // 新导师院系

    private String changeReason;       // 更换原因
    private String applyTime;          // 申请时间
    private String overallStatus;      // 整体状态
}
