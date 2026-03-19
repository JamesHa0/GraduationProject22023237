package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学籍异动申请表
 * @TableName student_status_change
 */
@TableName(value = "student_status_change")
@Data
public class StudentStatusChange {
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
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 异动类型：1-休学，2-复学，3-退学，4-延期毕业
     */
    private Integer changeType;

    /**
     * 异动原因
     */
    private String reason;

    /**
     * 附件路径
     */
    private String attachmentPath;

    /**
     * 原状态
     */
    private Integer originalStatus;

    /**
     * 新状态
     */
    private Integer newStatus;

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
     * 异动生效日期
     */
    private Date effectiveDate;

    /**
     * 申请时间
     */
    private Date applyTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
