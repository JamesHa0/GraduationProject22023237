package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学位论文中期检查表
 * @TableName thesis_midterm_check
 */
@TableName(value = "thesis_midterm_check")
@Data
public class ThesisMidterm {
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
     * 研究工作进展情况
     */
    private String progressReport;

    /**
     * 已完成工作
     */
    private String completedWork;

    /**
     * 未完成工作及原因
     */
    private String remainingWork;

    /**
     * 遇到的问题及解决方案
     */
    private String problems;

    /**
     * 下阶段工作计划
     */
    private String nextPlan;

    /**
     * 论文初稿完成情况
     */
    private Integer draftProgress;

    /**
     * 附件路径（中期检查报告）
     */
    private String attachmentPath;

    /**
     * 检查时间
     */
    private Date checkTime;

    /**
     * 检查地点
     */
    private String checkLocation;

    /**
     * 评审委员会成员
     */
    private String committeeMembers;

    /**
     * 导师审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer mentorStatus;

    /**
     * 导师检查意见
     */
    private String mentorComment;

    /**
     * 教学秘书审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer secretaryStatus;

    /**
     * 教学秘书审批意见
     */
    private String secretaryComment;

    /**
     * 分管院长审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer deanStatus;

    /**
     * 分管院长审批意见
     */
    private String deanComment;

    /**
     * 综合状态：0-未提交，1-导师审批中，2-秘书审批中，3-院长审批中，4-已通过，5-已拒绝
     */
    private Integer overallStatus;

    /**
     * 提交时间
     */
    private Date submitTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
