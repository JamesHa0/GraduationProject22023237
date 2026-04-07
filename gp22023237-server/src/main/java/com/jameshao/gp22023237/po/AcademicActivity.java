package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学术活动记录表（讲座、研讨会、论坛等）
 * @TableName academic_activity
 */
@TableName(value = "academic_activity")
@Data
public class AcademicActivity {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 活动类型：1-学术讲座，2-研讨会，3-论坛，4-其他
     */
    private Integer activityType;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动时间
     */
    private Date activityTime;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 主讲人/主持人
     */
    private String speaker;

    /**
     * 活动内容描述
     */
    private String content;

    /**
     * 导师审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer mentorStatus;

    /**
     * 导师审批意见
     */
    private String mentorComment;

    /**
     * 教学秘书审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer secretaryStatus;

    /**
     * 教学秘书审批意见
     */
    private String secretaryComment;

    /**
     * 分管院长审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer deanStatus;

    /**
     * 分管院长审批意见
     */
    private String deanComment;

    /**
     * 附件路径（证明材料）
     */
    private String attachmentPath;

    /**
     * 提交时间
     */
    private Date submitTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
