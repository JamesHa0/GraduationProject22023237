package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学位论文开题报告实体类
 * 用于存储研究生学位论文开题报告的详细信息
 * 包含论文基本信息、研究内容、审批状态等
 * 支持导师、教学秘书、分管院长三级审批流程
 * @TableName thesis_proposal
 */
@TableName(value = "thesis_proposal")
@Data
public class ThesisProposal {
    /**
     * 开题报告ID - 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID - 关联学生表
     */
    private Long studentId;

    /**
     * 学生姓名 - 冗余字段，便于展示
     */
    private String studentName;

    /**
     * 学号 - 冗余字段，便于展示
     */
    private String studentNo;

    /**
     * 导师ID - 关联导师表
     */
    private Long mentorId;

    /**
     * 导师姓名 - 冗余字段，便于展示
     */
    private String mentorName;

    /**
     * 论文题目 - 学生拟研究的学位论文题目
     */
    private String thesisTitle;

    /**
     * 研究背景与意义 - 阐述选题的背景、理论意义和实际应用价值
     */
    private String background;

    /**
     * 国内外研究现状 - 综述该领域的研究进展和存在的问题
     */
    private String researchStatus;

    /**
     * 研究内容与目标 - 明确主要研究内容和预期达到的研究目标
     */
    private String researchContent;

    /**
     * 研究方法与技术路线 - 说明采用的研究方法和技术实现路径
     */
    private String researchMethod;

    /**
     * 预期成果 - 描述预期取得的研究成果和创新点
     */
    private String expectedResults;

    /**
     * 研究计划与进度安排 - 分阶段说明研究工作的时间安排
     */
    private String researchPlan;

    /**
     * 参考文献 - 列出主要的参考文献，使用反引号避免SQL关键字冲突
     */
    @TableField("`references`")
    private String references;

    /**
     * 附件路径 - 开题报告文档的文件存储路径
     */
    private String attachmentPath;

    /**
     * 开题时间 - 开题报告会的具体时间
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
