package com.jameshao.gp22023237.DTO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 排课导入Excel DTO
 */
@Data
public class ScheduleImportDTO {

    @ExcelProperty("教师工号")
    private String teacherNo;

    @ExcelProperty("教师姓名")
    private String teacherName;

    @ExcelProperty("课程编号")
    private String courseNo;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("班级名称")
    private String className;

    @ExcelProperty("星期")
    private String dayOfWeek;

    @ExcelProperty("开始节次")
    private String startSection;

    @ExcelProperty("结束节次")
    private String endSection;

    @ExcelProperty("教室")
    private String classroom;

    @ExcelProperty("学期")
    private String semester;

    @ExcelProperty("学年")
    private String year;
}
