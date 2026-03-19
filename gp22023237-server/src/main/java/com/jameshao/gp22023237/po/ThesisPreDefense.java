package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学位论文预答辩表
 * @TableName thesis_pre_defense
 */
@TableName(value = "thesis_pre_defense")
@Data
public class ThesisPreDefense {
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
     * 论文摘要
     */
    private String abstractContent;

    /**
     * 论文初稿附件路径
     */
    private String attachmentPath;

    /**
     * 预答辩时间
     */
    private Date defenseTime;

    /**
     * 预答辩地点
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
     * 答辩记录（问题和回答）
     */
    private String qaRecord;

    /**
     * 导师审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer mentorStatus;

    /**
     * 导师审批意见
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
     * 预答辩结果：0-未开始，1-通过，2-不通过
     */
    private Integer defenseResult;

    /**
     * 答委评语
     */
    private String committeeComment;

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
