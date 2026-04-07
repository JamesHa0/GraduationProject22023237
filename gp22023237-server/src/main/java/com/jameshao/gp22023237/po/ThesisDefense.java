package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 论文答辩表
 * @TableName thesis_defense
 */
@TableName(value = "thesis_defense")
@Data
public class ThesisDefense {
    /**
     * 答辩ID
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
     * 论文终稿路径
     */
    private String thesisFinalUrl;

    /**
     * 导师审批：0-未审批，1-同意，2-拒绝
     */
    private Integer tutorApproval;

    /**
     * 审批导师ID
     */
    private Long tutorId;

    /**
     * 导师审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date tutorApprovalTime;

    /**
     * 院长审批：0-未审批，1-同意，2-拒绝
     */
    private Integer deanApproval;

    /**
     * 院长审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date deanApprovalTime;

    /**
     * 答辩日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date defenseDate;

    /**
     * 答辩地点
     */
    private String defenseLocation;

    /**
     * 答辩委员会主席
     */
    private String committeeChair;

    /**
     * 答辩委员会成员
     */
    private String committeeMembers;

    /**
     * 答辩结果：0-未开始，1-通过，2-修改后通过，3-不通过
     */
    private Integer defenseResult;

    /**
     * 答辩评分
     */
    private Double defenseScore;

    /**
     * 答辩委员会评语
     */
    private String defenseComments;

    /**
     * 问答记录
     */
    private String qaRecord;

    /**
     * 状态：0-未提交，1-待答辩，2-已完成
     */
    private Integer status;

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
