package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 毕业资格审核实体类
 * 数据库表: graduation_qualification
 * 注意: 字段名与数据库表结构保持一致，避免SQL错误
 */
@TableName(value = "graduation_qualification")
@Data
public class GraduationAudit {
    /**
     * 主键ID - 自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID - 关联学生表，数据库字段名: student_id
     */
    private Long studentId;

    /**
     * 学分检查 - 数据库字段名: credit_check
     * 0-未通过，1-通过
     */
    @TableField("credit_check")
    private Integer creditsPass;

    /**
     * 论文检查 - 数据库字段名: thesis_check
     * 0-未通过，1-通过
     */
    @TableField("thesis_check")
    private Integer thesisPass;

    /**
     * 实践检查 - 数据库字段名: practice_check
     * 0-未通过，1-通过
     */
    @TableField("practice_check")
    private Integer practicePass;

    /**
     * 整体结果 - 数据库字段名: overall_result
     * 0-待审核，1-通过，2-不通过
     */
    @TableField("overall_result")
    private Integer auditStatus;

    /**
     * 审核人ID - 数据库字段名: reviewer_id
     */
    @TableField("reviewer_id")
    private Long auditorId;

    /**
     * 审核时间 - 数据库字段名: review_time
     */
    @TableField("review_time")
    private Date auditTime;

    /**
     * 审核意见 - 数据库字段名: comment
     */
    private String comment;

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
    // 这些字段的值需要从其他表关联查询获取，或者使用@TableField(exist=false)标注

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
     * 已修总学分 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private java.math.BigDecimal totalCredits;

    /**
     * 要求总学分 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private java.math.BigDecimal requiredCredits;

    /**
     * 学分是否达标 - 数据库中不存在此字段，使用creditsPass代替
     */
    @TableField(exist = false)
    private Integer creditCheck;

    /**
     * 开题报告状态 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private Integer thesisProposalPass;

    /**
     * 中期检查状态 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private Integer midtermPass;

    /**
     * 预答辩状态 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private Integer preDefensePass;

    /**
     * 答辩状态 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private Integer defensePass;

    /**
     * 学术成果是否达标 - 数据库中不存在此字段
     */
    @TableField(exist = false)
    private Integer academicPass;

    /**
     * 审核意见 - 数据库中不存在此字段，使用comment代替
     */
    @TableField(exist = false)
    private String auditComment;

    /**
     * 审核人姓名 - 数据库中不存在此字段，需从User表关联获取
     */
    @TableField(exist = false)
    private String auditorName;

    /**
     * 申请时间 - 数据库中不存在此字段，使用createTime代替
     */
    @TableField(exist = false)
    private Date applyTime;
}
