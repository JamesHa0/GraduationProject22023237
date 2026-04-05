package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 选课记录表
 * @TableName course_selection
 */
@TableName(value ="course_selection")
@Data
public class CourseSelection {
    /**
     * 选课记录ID
     */
    @TableId(type = IdType.AUTO)
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
    private Date selectionTime;

    /**
     * 提交时间，不为空表示已提交
     */
    private Date submitTime;

    /**
     * 状态：1-正常，0-已退课
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
