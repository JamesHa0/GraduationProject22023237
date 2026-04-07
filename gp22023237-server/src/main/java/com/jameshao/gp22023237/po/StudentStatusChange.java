package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学籍异动申请表实体类
 * 数据库表: student_status_change
 * 注意: 字段名与数据库表结构保持一致，避免SQL错误
 */
@TableName(value = "student_status_change")
@Data
public class StudentStatusChange {
    /**
     * 主键ID - 自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID - 关联学生表
     */
    private Long studentId;

    /**
     * 异动类型 - 数据库字段名: type
     * 1-休学，2-复学，3-退学，4-延期毕业
     */
    @TableField("type")
    private Integer changeType;

    /**
     * 异动原因 - 数据库字段名: reason
     */
    private String reason;

    /**
     * 开始日期 - 数据库字段名: start_date
     */
    private Date startDate;

    /**
     * 结束日期 - 数据库字段名: end_date (可为空)
     */
    private Date endDate;

    /**
     * 导师审批状态 - 数据库字段名: tutor_approval
     * 0-未审批，1-同意，2-拒绝
     */
    @TableField("tutor_approval")
    private Integer mentorStatus;

    /**
     * 导师ID - 数据库字段名: tutor_id
     */
    @TableField("tutor_id")
    private Long mentorId;

    /**
     * 导师审批时间 - 数据库字段名: tutor_approval_time
     */
    @TableField("tutor_approval_time")
    private Date mentorApprovalTime;

    /**
     * 辅导员审批状态 - 数据库字段名: counselor_approval
     * 0-未审批，1-同意，2-拒绝
     */
    @TableField("counselor_approval")
    private Integer secretaryStatus;

    /**
     * 辅导员ID - 数据库字段名: counselor_id
     */
    @TableField("counselor_id")
    private Long secretaryId;

    /**
     * 辅导员审批时间 - 数据库字段名: counselor_approval_time
     */
    @TableField("counselor_approval_time")
    private Date secretaryApprovalTime;

    /**
     * 整体状态 - 数据库字段名: status
     * 0-待审批，1-审批中，2-已通过，3-已拒绝
     */
    private Integer status;

    /**
     * 创建时间 - 数据库字段名: create_time
     */
    private Date createTime;

    /**
     * 更新时间 - 数据库字段名: update_time
     */
    private Date updateTime;

    // ===================== 兼容性字段 (用于前后端过渡) =====================
    // 下面的字段在数据库中不存在，但为了保持API兼容性，暂保留
    // 这些字段的值需要从Student表关联查询获取，或者使用@TableField(exist=false)标注

    /**
     * 学生姓名 - 数据库中不存在此字段，需从Student表关联获取
     */
    @TableField(exist = false)
    private String studentName;

    /**
     * 学号 - 数据库中不存在此字段，需从Student表关联获取
     */
    @TableField(exist = false)
    private String studentNo;

    /**
     * 附件路径 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private String attachmentPath;

    /**
     * 原状态 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private Integer originalStatus;

    /**
     * 新状态 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private Integer newStatus;

    /**
     * 导师审批意见 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private String mentorComment;

    /**
     * 教学秘书审批意见 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private String secretaryComment;

    /**
     * 分管院长审批状态 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private Integer deanStatus;

    /**
     * 分管院长审批意见 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private String deanComment;

    /**
     * 异动生效日期 - 数据库中不存在此字段，使用startDate代替
     */
    @TableField(exist = false)
    private Date effectiveDate;

    /**
     * 申请时间 - 数据库中不存在此字段，使用createTime代替
     */
    @TableField(exist = false)
    private Date applyTime;
}
