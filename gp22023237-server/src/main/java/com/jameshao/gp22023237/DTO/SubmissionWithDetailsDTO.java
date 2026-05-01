package com.jameshao.gp22023237.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 提交主表 + 子表详情 + 审批记录 联查DTO
 * 替代原来的三个 WithDetailsDTO
 */
@Data
public class SubmissionWithDetailsDTO {

    // ========== 主表字段 ==========
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long submitterId;
    private Integer submitterType;
    private Integer contentType;
    private String title;
    private String abstractContent;
    private String fileUrls;
    private Integer approvalStatus;
    private Long currentApproverId;
    private Integer currentApproverType;
    private Date submitTime;
    private Integer isDeleted;
    private Integer version;
    private Date createTime;
    private Date updateTime;

    // ========== 学术活动详情 ==========
    private Integer activityType;
    private String activityName;
    private Date activityTime;
    private String location;
    private String speaker;
    private String content;

    // ========== 学术成果详情 ==========
    private Integer achievementType;
    private String authors;
    private Date publicationDate;
    private String journalName;
    private Integer journalLevel;
    private String volume;
    private String issue;
    private String pages;
    private String doi;
    private String patentNo;
    private Integer patentType;
    private Integer patentStatus;
    private String awardName;
    private Integer awardLevel;
    private String awardIssuer;
    private String projectName;
    private Integer projectRole;

    // ========== 创新创业详情 ==========
    private Integer projectType;
    private Integer projectLevel;
    private String projectNo;
    private String leader;
    private String members;
    private String advisor;
    private Date startDate;
    private Date endDate;
    private String description;
    private String achievements;
    private Integer innovationAwardLevel;
    private BigDecimal fundingAmount;

    // ========== 审批记录列表（非SQL映射，Service层填充） ==========
    private List<ApprovalRecordDTO> approvalRecords;

    // ========== 兼容前端字段 ==========

    /** 兼容前端：获取统一类型值 */
    public Integer getType() {
        if (contentType != null) {
            switch (contentType) {
                case 1: return activityType;
                case 2: return achievementType;
                case 3: return projectType;
            }
        }
        return null;
    }

    /** 兼容前端：获取标题 */
    public String getTitle() {
        if (title != null) return title;
        if (contentType != null) {
            switch (contentType) {
                case 1: return activityName;
                case 3: return projectName;
            }
        }
        return title;
    }

    /** 兼容前端：获取时间 */
    public Date getTime() {
        if (contentType != null) {
            switch (contentType) {
                case 1: return activityTime;
                case 2: return publicationDate;
                case 3: return startDate;
            }
        }
        return submitTime;
    }

    /** 兼容前端：获取描述 */
    public String getDescription() {
        if (contentType != null) {
            switch (contentType) {
                case 1: return content;
                case 2: return abstractContent;
                case 3: return description;
            }
        }
        return abstractContent;
    }

    /** 兼容前端：获取附件路径（旧单文件格式） */
    public String getAttachmentPath() {
        return fileUrls;
    }

    /** 兼容前端：导师审批状态（从 approvalStatus 推算） */
    public Integer getMentorStatus() {
        if (approvalStatus == null) return 0;
        switch (approvalStatus) {
            case 0: return 0; // 待提交
            case 1: return 0; // 待导师审批 → 导师未审批
            case 2: return 1; // 待秘书审批 → 导师已通过
            case 3: return 1; // 待院长审批 → 导师已通过
            case 4: return 1; // 已通过 → 导师已通过
            case 5: // 已驳回：根据currentApproverType判断驳回节点
                // currentApproverType=2(导师)说明导师驳回，导师状态=2(已拒绝)
                // currentApproverType>2说明导师已通过，但在后续节点被驳回
                if (currentApproverType != null && currentApproverType == 2) return 2;
                return 1; // 后续节点驳回，导师已通过
            default: return 0;
        }
    }

    /** 兼容前端：秘书审批状态 */
    public Integer getSecretaryStatus() {
        if (approvalStatus == null) return 0;
        switch (approvalStatus) {
            case 0: case 1: return 0;
            case 2: return 0; // 待秘书审批
            case 3: case 4: return 1; // 秘书已通过
            case 5: // 已驳回：根据currentApproverType判断
                if (currentApproverType != null && currentApproverType == 2) return 0; // 导师阶段驳回，秘书未审批
                if (currentApproverType != null && currentApproverType == 5) return 2; // 秘书驳回
                return 1; // 院长阶段驳回，秘书已通过
            default: return 0;
        }
    }

    /** 兼容前端：院长审批状态 */
    public Integer getDeanStatus() {
        if (approvalStatus == null) return 0;
        switch (approvalStatus) {
            case 0: case 1: case 2: return 0;
            case 3: return 0; // 待院长审批
            case 4: return 1; // 院长已通过
            case 5: // 已驳回：根据currentApproverType判断
                if (currentApproverType != null && currentApproverType == 1) return 2; // 院长驳回
                if (currentApproverType != null && (currentApproverType == 2 || currentApproverType == 5)) return 0; // 导师/秘书阶段驳回
                return 0;
            default: return 0;
        }
    }

    /** 兼容前端：获取主讲人/主办方 */
    public String getOrganizer() {
        if (contentType != null) {
            switch (contentType) {
                case 1: return speaker;
                case 3: return advisor;
            }
        }
        return null;
    }

    /** 兼容前端：获取审批人审批状态 */
    public Integer getApproverStatus() {
        return getMentorStatus();
    }

    /** 兼容前端：获取秘书审批状态 */
    public Integer getSecretaryApproval() {
        return getSecretaryStatus();
    }
}
