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

    @ExcelProperty("授课教师工号")
    private String teacherNo;

    @ExcelProperty("学期")
    private String semester;

    @ExcelProperty("学年")
    private Integer year;

    @ExcelProperty("最大选课人数")
    private Integer maxStudents;

    @ExcelProperty("星期几")
    private Integer dayOfWeek;

    @ExcelProperty("开始时间")
    private String startTime;

    @ExcelProperty("结束时间")
    private String endTime;

    @ExcelProperty("教室")
    private String classroom;

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
