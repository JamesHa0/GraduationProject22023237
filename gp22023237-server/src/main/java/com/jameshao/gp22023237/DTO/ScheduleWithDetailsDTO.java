package com.jameshao.gp22023237.DTO;

import lombok.Data;

/**
 * 排课详情DTO（含课程名/教师名/班级名/时间片名称）
 */
@Data
public class ScheduleWithDetailsDTO {
    /**
     * 排课ID
     */
    private Long id;

    /**
     * 授课教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 开始节次(dict_code)
     */
    private Long startSection;

    /**
     * 结束节次(dict_code)
     */
    private Long endSection;

    /**
     * 开始节次名称（如：第1节）
     */
    private String startSectionName;

    /**
     * 结束节次名称（如：第2节）
     */
    private String endSectionName;

    /**
     * 开始节次时间（如：08:00-09:00）
     */
    private String startSectionValue;

    /**
     * 结束节次时间（如：09:00-10:00）
     */
    private String endSectionValue;

    /**
     * 节次显示文本（如：第1-2节）
     */
    private String sectionDisplay;

    /**
     * 星期几(1-7)
     */
    private Integer dayOfWeek;

    /**
     * 教室
     */
    private String classroom;

    /**
     * 学期
     */
    private String semester;

    /**
     * 学年
     */
    private Integer year;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;
}
