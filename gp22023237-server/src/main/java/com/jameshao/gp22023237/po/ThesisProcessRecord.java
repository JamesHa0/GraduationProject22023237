package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 论文流程记录表
 * 存储论文各环节的流程记录（开题、中期、预答辩、外审、答辩等）
 * @TableName thesis_process_record
 */
@TableName(value = "thesis_process_record")
@Data
public class ThesisProcessRecord {
    /**
     * 流程记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联论文主ID
     */
    private Long thesisId;

    /**
     * 流程环节类型：1-开题报告，2-中期检查，3-预答辩，4-论文外审，5-正式答辩，6-二次答辩，7-修改后再审
     */
    private Integer processType;

    /**
     * 版本号：支持同一环节多次提交/二次答辩
     */
    private Integer version;

    /**
     * 本环节提交的论文版本路径
     */
    private String thesisVersionUrl;

    /**
     * 本环节附件路径
     */
    private String attachmentUrl;

    /**
     * 环节个性化内容（JSON格式）
     */
    private String contentExtend;

    /**
     * 导师审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer supervisorStatus;

    /**
     * 导师审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date supervisorTime;

    /**
     * 导师审批意见
     */
    private String supervisorComment;

    /**
     * 导师审批操作人ID
     */
    private Long supervisorApproverId;

    /**
     * 教学秘书审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer secretaryStatus;

    /**
     * 秘书审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date secretaryTime;

    /**
     * 秘书审批意见
     */
    private String secretaryComment;

    /**
     * 秘书审批操作人ID
     */
    private Long secretaryApproverId;

    /**
     * 院长审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer deanStatus;

    /**
     * 院长审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date deanTime;

    /**
     * 院长审批意见
     */
    private String deanComment;

    /**
     * 院长审批操作人ID
     */
    private Long deanApproverId;

    /**
     * 事件时间（开题/答辩/外审时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date eventTime;

    /**
     * 事件地点
     */
    private String eventLocation;

    /**
     * 评审委员会主席
     */
    private String reviewCommitteeChair;

    /**
     * 评审/外审专家成员
     */
    private String reviewCommitteeMembers;

    /**
     * 评审/答辩结果：0-未进行，1-通过，2-修改后通过，3-未通过
     */
    private Integer reviewResult;

    /**
     * 评审/答辩评分
     */
    private BigDecimal reviewScore;

    /**
     * 评审/答辩委员会评语
     */
    private String reviewComment;

    /**
     * 答辩问答记录
     */
    private String qaRecord;

    /**
     * 环节整体状态：0-未提交，1-审批中，2-评审中，3-已通过，4-已拒绝，5-已完成
     */
    private Integer processStatus;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
