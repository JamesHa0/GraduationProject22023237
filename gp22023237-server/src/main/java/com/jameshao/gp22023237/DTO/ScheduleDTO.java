package com.jameshao.gp22023237.DTO;

import lombok.Data;

import java.util.List;

/**
 * 排课提交DTO（支持多班级批量排课）
 */
@Data
public class ScheduleDTO {
    /**
     * 排课ID（编辑时传入）
     */
    private Long id;

    /**
     * 授课教师ID
     */
    private Long teacherId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 班级ID列表（支持多选班级批量排课）
     */
    private List<Long> classIds;

    /**
     * 单个班级ID（编辑时使用）
     */
    private Long classId;

    /**
     * 开始节次(dict_code)
     */
    private Long startSection;

    /**
     * 结束节次(dict_code)
     */
    private Long endSection;

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
}
