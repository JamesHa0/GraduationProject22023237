package com.jameshao.gp22023237.DTO;

import lombok.Data;

/**
 * 选课记录带课程详情DTO
 */
@Data
public class CourseSelectionWithDetailsDTO {
    /**
     * 选课记录ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 选课时间
     */
    private String selectionTime;

    /**
     * 状态：1-正常，0-已退课
     */
    private Integer status;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 学分
     */
    private Double credit;

    /**
     * 学时
     */
    private Integer hours;

    /**
     * 授课教师ID
     */
    private Long teacherId;

    /**
     * 授课教师姓名
     */
    private String teacherName;

    /**
     * 学期
     */
    private String semester;

    /**
     * 学年
     */
    private Object year;

    /**
     * 最大选课人数
     */
    private Integer maxStudents;

    /**
     * 星期几(1-7)
     */
    private Integer dayOfWeek;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 教室
     */
    private String classroom;

    /**
     * 最大学分限制
     */
    private Double maxCredits;

    /**
     * 课程状态
     */
    private Integer courseStatus;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 修读性质
     */
    private String studyNature;

    /**
     * 教材：1-是，0-否
     */
    private Integer textbook;

    /**
     * 外年级/专业选课：1-是，0-否
     */
    private Integer externalSelection;

    /**
     * 备注
     */
    private String remark;
}
