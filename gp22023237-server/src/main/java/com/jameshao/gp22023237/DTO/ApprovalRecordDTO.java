package com.jameshao.gp22023237.DTO;

import lombok.Data;

import java.util.Date;

/**
 * 审批记录DTO
 */
@Data
public class ApprovalRecordDTO {

    private Long id;

    /** 关联提交主表ID */
    private Long submissionId;

    /** 审批人ID */
    private Long approverId;

    /** 审批人姓名 */
    private String approverName;

    /** 审批人类型：2=导师，5=教学秘书，1=分管院长 */
    private Integer approverType;

    /** 审批人类型名称 */
    private String approverTypeName;

    /** 审批动作：1=提交，2=通过，3=驳回，4=撤回 */
    private Integer approvalAction;

    /** 审批动作名称 */
    private String approvalActionName;

    /** 审批意见 */
    private String approvalComment;

    /** 审批人附件URL列表（JSON数组） */
    private String reviewerFileUrls;

    /** 审批时间 */
    private Date approvalTime;

    private Date createTime;
}
