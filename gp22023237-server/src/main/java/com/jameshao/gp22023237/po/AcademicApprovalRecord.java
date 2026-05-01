package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 审批流程记录表
 * @TableName academic_approval_record
 */
@TableName(value = "academic_approval_record")
@Data
public class AcademicApprovalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联提交主表ID */
    private Long submissionId;

    /** 审批人ID（关联teacher表或user表） */
    private Long approverId;

    /** 审批人类型：2=导师，5=教学秘书，1=分管院长 */
    private Integer approverType;

    /** 审批动作：1=提交，2=通过，3=驳回，4=撤回 */
    private Integer approvalAction;

    /** 审批意见 */
    private String approvalComment;

    /** 审批人附件URL列表（JSON数组） */
    private String reviewerFileUrls;

    /** 审批时间 */
    private Date approvalTime;

    private Date createTime;
}
