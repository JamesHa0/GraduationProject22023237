package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 创新创业项目表
 * @TableName innovation_project
 */
@TableName(value = "innovation_project")
@Data
public class InnovationProject {
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
     * 项目类型：1-创新项目，2-创业项目，3-竞赛
     */
    private Integer projectType;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目级别：1-国家级，2-省级，3-市级，4-校级
     */
    private Integer projectLevel;

    /**
     * 项目编号
     */
    private String projectNo;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 参与成员
     */
    private String members;

    /**
     * 指导老师
     */
    private String advisor;

    /**
     * 项目开始时间
     */
    private Date startDate;

    /**
     * 项目结束时间
     */
    private Date endDate;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目成果/获奖情况
     */
    private String achievements;

    /**
     * 获奖等级（如有）：1-特等奖，2-一等奖，3-二等奖，4-三等奖，5-优秀奖
     */
    private Integer awardLevel;

    /**
     * 资助金额（元）
     */
    private BigDecimal fundingAmount;

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
     * 附件路径
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
