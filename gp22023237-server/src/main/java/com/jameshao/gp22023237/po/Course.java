package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 课程表
 * @TableName course
 */
@TableName(value ="course")
@Data
public class Course {
    /**
     * 课程ID
     */
    @TableId(type = IdType.AUTO)
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
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
