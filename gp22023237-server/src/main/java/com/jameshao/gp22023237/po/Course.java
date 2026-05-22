package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 课程容量（最多选课人数），不设置时默认不限制
     */
    private Integer capacity;

    /**
     * 学期
     */
    private String semester;

    /**
     * 学年
     */
    private Integer year;

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
