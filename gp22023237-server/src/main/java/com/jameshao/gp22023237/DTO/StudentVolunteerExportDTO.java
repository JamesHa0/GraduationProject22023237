package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 学生志愿表导出DTO
 */
@Data
public class StudentVolunteerExportDTO {
    private String studentNo;          // 学号
    private String studentName;        // 学生姓名
    private String department;         // 院系
    private String major;              // 专业
    private String admissionYear;      // 年级

    // 志愿列表（每个志愿包含：志愿序号、导师姓名、职称、研究领域、选择状态）
    private List<Map<String, String>> volunteers;
}
