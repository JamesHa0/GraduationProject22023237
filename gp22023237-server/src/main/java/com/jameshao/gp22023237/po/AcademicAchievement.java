package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学术成果表（论文、专利、科研奖励等）
 * @TableName academic_achievement
 */
@TableName(value = "academic_achievement")
@Data
public class AcademicAchievement {
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
     * 成果类型：1-论文，2-专利，3-科研奖励，4-项目参与
     */
    private Integer achievementType;

    /**
     * 成果标题
     */
    private String title;

    /**
     * 作者/参与者
     */
    private String authors;

    /**
     * 发表/授权时间
     */
    private Date publicationDate;

    /**
     * 论文期刊名称（论文类型）
     */
    private String journalName;

    /**
     * 期刊级别：1-SCI/EI，2-核心期刊，3-普通期刊
     */
    private Integer journalLevel;

    /**
     * 卷号
     */
    private String volume;

    /**
     * 期号
     */
    private String issue;

    /**
     * 页码
     */
    private String pages;

    /**
     * DOI/链接
     */
    private String doi;

    /**
     * 专利号（专利类型）
     */
    private String patentNo;

    /**
     * 专利类型：1-发明专利，2-实用新型，3-外观设计
     */
    private Integer patentType;

    /**
     * 授权状态：0-申请中，1-已授权
     */
    private Integer patentStatus;

    /**
     * 奖励名称（科研奖励类型）
     */
    private String awardName;

    /**
     * 奖励级别：1-国家级，2-省级，3-市级，4-校级
     */
    private Integer awardLevel;

    /**
     * 发奖单位
     */
    private String awardIssuer;

    /**
     * 项目名称（项目参与类型）
     */
    private String projectName;

    /**
     * 项目角色：1-负责人，2-核心成员，3-参与者
     */
    private Integer projectRole;

    /**
     * 摘要/描述
     */
    private String abstractContent;

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
     * 附件路径（证明材料）
     */
    private String attachmentPath;

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
