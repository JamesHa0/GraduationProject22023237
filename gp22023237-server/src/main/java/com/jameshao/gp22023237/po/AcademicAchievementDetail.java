package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 学术成果详情子表
 * @TableName academic_achievement_detail
 */
@TableName(value = "academic_achievement_detail")
@Data
public class AcademicAchievementDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联提交主表ID */
    private Long submissionId;

    /** 成果类型：1=论文，2=专利，3=科研奖励，4=项目参与 */
    private Integer achievementType;

    /** 作者/参与者 */
    private String authors;

    /** 发表/授权时间 */
    private Date publicationDate;

    /** 论文期刊名称 */
    private String journalName;

    /** 期刊级别：1=SCI/EI，2=核心期刊，3=普通期刊 */
    private Integer journalLevel;

    /** 卷号 */
    private String volume;

    /** 期号 */
    private String issue;

    /** 页码 */
    private String pages;

    /** DOI号 */
    private String doi;

    /** 专利号/软著登记号 */
    private String patentNo;

    /** 专利类型：1=发明，2=实用新型，3=外观设计 */
    private Integer patentType;

    /** 授权状态：0=申请中，1=已授权 */
    private Integer patentStatus;

    /** 奖励名称 */
    private String awardName;

    /** 奖励级别：1=国家级，2=省级，3=市级，4=校级 */
    private Integer awardLevel;

    /** 发奖单位 */
    private String awardIssuer;

    /** 项目名称 */
    private String projectName;

    /** 项目角色：1=负责人，2=核心成员，3=参与者 */
    private Integer projectRole;
}
