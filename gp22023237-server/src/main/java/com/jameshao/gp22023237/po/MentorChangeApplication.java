package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 导师更换申请表实体类
 * 数据库表: mentor_change_application
 */
@TableName(value = "mentor_change_application")
@Data
public class MentorChangeApplication {

    /**
     * 主键ID - 自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 原导师ID
     */
    private Long originalMentorId;

    /**
     * 新导师ID
     */
    private Long newMentorId;

    /**
     * 更换原因
     */
    private String changeReason;

    /**
     * 原导师审批状态
     * 0-待审批，1-已通过，2-已拒绝
     */
    private Integer originalMentorStatus;

    /**
     * 原导师审批意见
     */
    private String originalMentorComment;

    /**
     * 原导师审批时间
     */
    private Date originalMentorTime;

    /**
     * 新导师审批状态
     * 0-待审批，1-已通过，2-已拒绝
     */
    private Integer newMentorStatus;

    /**
     * 新导师审批意见
     */
    private String newMentorComment;

    /**
     * 新导师审批时间
     */
    private Date newMentorTime;

    /**
     * 整体状态
     * 0-待审批(待原导师)，1-待新导师审批，2-已通过，3-已拒绝
     */
    private Integer overallStatus;

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

    // ===================== 兼容性字段 (用于前后端过渡) =====================

    /**
     * 学生姓名 - 数据库中不存在此字段，需关联查询获取
     */
    @TableField(exist = false)
    private String studentName;

    /**
     * 学号 - 数据库中不存在此字段，需关联查询获取
     */
    @TableField(exist = false)
    private String studentNo;

    /**
     * 原导师姓名 - 数据库中不存在此字段，需关联查询获取
     */
    @TableField(exist = false)
    private String originalMentorName;

    /**
     * 原导师工号 - 数据库中不存在此字段，需关联查询获取
     */
    @TableField(exist = false)
    private String originalMentorNo;

    /**
     * 新导师姓名 - 数据库中不存在此字段，需关联查询获取
     */
    @TableField(exist = false)
    private String newMentorName;

    /**
     * 新导师工号 - 数据库中不存在此字段，需关联查询获取
     */
    @TableField(exist = false)
    private String newMentorNo;
}
