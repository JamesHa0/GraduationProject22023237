package com.jameshao.gp22023237.DTO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 学生导入DTO（Excel/CSV统一字段）
 */
@Data
public class StudentImportDTO {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("学院")
    private String department;

    @ExcelProperty("专业")
    private String major;

    @ExcelProperty("入学年份")
    private Integer admissionYear;

    @ExcelProperty("归属年级")
    private Integer cohortYear;

    @ExcelProperty("毕业年份")
    private Integer graduationYear;

    @ExcelProperty("研究方向")
    private String researchDirection;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelProperty("双选状态")
    private Integer selectionStatus;
}
