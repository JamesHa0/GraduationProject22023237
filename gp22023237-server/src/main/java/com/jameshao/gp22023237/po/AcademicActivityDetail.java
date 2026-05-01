package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 学术活动详情子表
 * @TableName academic_activity_detail
 */
@TableName(value = "academic_activity_detail")
@Data
public class AcademicActivityDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联提交主表ID */
    private Long submissionId;

    /** 活动类型：1=学术讲座，2=研讨会，3=论坛，4=其他 */
    private Integer activityType;

    /** 活动名称 */
    private String activityName;

    /** 活动时间 */
    private Date activityTime;

    /** 活动地点 */
    private String location;

    /** 主讲人/主持人 */
    private String speaker;

    /** 活动内容描述 */
    private String content;
}
