package com.jameshao.gp22023237.DTO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 课程导入Excel DTO
 */
@Data
public class CourseImportDTO {

    @ExcelProperty("课程编号")
    private String courseNo;

    @ExcelProperty("课程名称")
    private String name;

    @ExcelProperty("学分")
    private Double credit;

    @ExcelProperty("学时")
    private Integer hours;

    @ExcelProperty("学期")
    private String semester;

    @ExcelProperty("学年")
    private Integer year;

    @ExcelProperty("最大学分限制")
    private Double maxCredits;

    @ExcelProperty("课程状态")
    private Integer status;

    @ExcelProperty("修读性质")
    private String studyNature;

    @ExcelProperty("教材")
    private Integer textbook;

    @ExcelProperty("外年级选课")
    private Integer externalSelection;

    @ExcelProperty("课程描述")
    private String description;

    @ExcelProperty("备注")
    private String remark;
}
