package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 开题报告表
 * @TableName thesis_proposal
 */
@TableName(value = "thesis_proposal")
@Data
public class ThesisProposal {
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
     * 研究背景与意义
     */
    private String background;

    /**
     * 国内外研究现状
     */
    private String researchStatus;

    /**
     * 研究内容与目标
     */
    private String researchContent;

    /**
     * 研究方法与技术路线
     */
    private String researchMethod;

    /**
     * 预期成果
     */
    private String expectedResults;

    /**
     * 研究计划与进度安排
     */
    private String researchPlan;

    /**
     * 参考文献
     */
    @TableField("`references`")
    private String references;

    /**
     * 附件路径（开题报告文档）
     */
    private String attachmentPath;

    /**
     * 开题时间
     */
    private Date proposalTime;

    /**
     * 开题地点
     */
    private String proposalLocation;

    /**
     * 评审委员会成员
     */
    private String committeeMembers;

    /**
     * 导师审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer mentorStatus;

    /**
     * 导师修改意见
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
