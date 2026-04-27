package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 导师学生关系汇总表导出DTO
 */
@Data
public class MentorStudentSummaryExportDTO {
    private String exportDate;         // 导出日期

    // 汇总列表（每条记录包含：序号、学号、学生姓名、院系、专业、导师姓名、导师院系、研究领域、确认时间）
    private List<Map<String, String>> relationships;
}
