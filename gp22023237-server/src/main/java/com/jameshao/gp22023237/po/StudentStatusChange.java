package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学生学籍异动申请表
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
     * 异动类型：1-休学，2-复学，3-退学，4-转专业
     */
    private Integer type;

    /**
     * 申请原因
     */
    private String reason;

    /**
     * 开始日期
     */
    private Date startDate;

    /**
     * 结束日期（休学时有效）
     */
    private Date endDate;

    /**
     * 导师审核状态：0-未审核，1-通过，2-拒绝
     */
    private Integer tutorApproval;

    /**
     * 导师ID
     */
    private Long tutorId;

    /**
     * 导师审核时间
     */
    private Date tutorApprovalTime;

    /**
     * 辅导员/教学秘书审核状态：0-未审核，1-通过，2-拒绝
     */
    private Integer counselorApproval;

    /**
     * 辅导员/教学秘书ID
     */
    private Long counselorId;

    /**
     * 辅导员/教学秘书审核时间
     */
    private Date counselorApprovalTime;

    /**
     * 申请状态：0-待审核，1-审核中，2-已通过，3-已拒绝，4-已取消
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
