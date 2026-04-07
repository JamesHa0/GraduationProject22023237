package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 论文进展表
 * 存储所有论文阶段信息（开题、中期、预答辩）
 * @TableName thesis_progress
 */
@TableName(value ="thesis_progress")
@Data
public class ThesisProgress {
    /**
     * 进展ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 导师ID
     */
    private Long mentorId;

    /**
     * 导师姓名
     */
    private String mentorName;

    /**
     * 论文题目
     */
    private String thesisTitle;

    /**
     * 进展类型：1-开题，2-中期，3-预答辩
     */
    private Integer progressType;

    /**
     * 阶段内容（JSON格式）
     */
    private String content;

    /**
     * 导师审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer mentorStatus;

    /**
     * 导师审批意见
     */
    private String mentorComment;

    /**
     * 导师审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date mentorTime;

    /**
     * 秘书审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer secretaryStatus;

    /**
     * 秘书审批意见
     */
    private String secretaryComment;

    /**
     * 秘书审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date secretaryTime;

    /**
     * 院长审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer deanStatus;

    /**
     * 院长审批意见
     */
    private String deanComment;

    /**
     * 院长审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date deanTime;

    /**
     * 综合状态：0-未提交，1-导师审批中，2-秘书审批中，3-院长审批中，4-已通过，5-已拒绝
     */
    private Integer overallStatus;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;

    /**
     * 事件时间（开题时间、检查时间、预答辩时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date eventTime;

    /**
     * 事件地点
     */
    private String location;

    /**
     * 评审委员会成员
     */
    private String committeeMembers;

    /**
     * 附件路径
     */
    private String attachmentPath;

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
