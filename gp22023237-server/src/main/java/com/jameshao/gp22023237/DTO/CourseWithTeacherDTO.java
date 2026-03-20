package com.jameshao.gp22023237.DTO;

import lombok.Data;

/**
 * 课程带教师信息DTO
 */
@Data
public class CourseWithTeacherDTO {
    /**
     * 课程ID
     */
    private Long id;

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
     * 状态：0-未开课，1-已开课，2-已结课
     */
    private Integer status;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 已选人数 (用于选课展示)
     */
    private Integer selectedCount;
}
