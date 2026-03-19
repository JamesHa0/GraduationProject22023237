package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 毕业资格审核表
 * @TableName graduation_audit
 */
@TableName(value = "graduation_audit")
@Data
public class GraduationAudit {
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
     * 已修总学分
     */
    private BigDecimal totalCredits;

    /**
     * 要求总学分
     */
    private BigDecimal requiredCredits;

    /**
     * 学分是否达标
     */
    private Integer creditsPass;

    /**
     * 开题报告状态：0-未通过，1-通过
     */
    private Integer thesisProposalPass;

    /**
     * 中期检查状态：0-未通过，1-通过
     */
    private Integer midtermPass;

    /**
     * 预答辩状态：0-未通过，1-通过
     */
    private Integer preDefensePass;

    /**
     * 答辩状态：0-未通过，1-通过
     */
    private Integer defensePass;

    /**
     * 论文是否合格
     */
    private Integer thesisPass;

    /**
     * 学术成果是否达标（至少1篇论文或1项专利）
     */
    private Integer academicPass;

    /**
     * 综合审核状态：0-审核中，1-通过，2-不通过
     */
    private Integer auditStatus;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 审核人
     */
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 审核时间
     */
    private Date auditTime;

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
