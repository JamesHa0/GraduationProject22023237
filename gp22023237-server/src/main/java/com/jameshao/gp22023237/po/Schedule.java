package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;

/**
 * 排课计划表
 * @TableName schedule
 */
@TableName(value ="schedule")
@Data
public class Schedule {
    /**
     * 排课ID
     */
    @TableId(type = IdType.AUTO)
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
     * 班级ID
     */
    private Long classId;

    /**
     * 开始节次(关联sys_dict_data.dict_code，dict_type=sys_time_slot)
     */
    private Long startSection;

    /**
     * 结束节次(关联sys_dict_data.dict_code，dict_type=sys_time_slot)
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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
