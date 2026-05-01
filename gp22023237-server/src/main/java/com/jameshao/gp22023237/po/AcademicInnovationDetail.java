package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 创新创业项目详情子表
 * @TableName academic_innovation_detail
 */
@TableName(value = "academic_innovation_detail")
@Data
public class AcademicInnovationDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联提交主表ID */
    private Long submissionId;

    /** 项目类型：1=创新项目，2=创业项目，3=竞赛 */
    private Integer projectType;

    /** 项目名称 */
    private String projectName;

    /** 项目级别：1=国家级，2=省级，3=市级，4=校级 */
    private Integer projectLevel;

    /** 项目编号 */
    private String projectNo;

    /** 负责人 */
    private String leader;

    /** 参与成员 */
    private String members;

    /** 指导老师 */
    private String advisor;

    /** 项目开始时间 */
    private Date startDate;

    /** 项目结束时间 */
    private Date endDate;

    /** 项目描述 */
    private String description;

    /** 项目成果/获奖情况 */
    private String achievements;

    /** 获奖等级：1=特等奖，2=一等奖，3=二等奖，4=三等奖，5=优秀奖 */
    private Integer awardLevel;

    /** 资助金额（元） */
    private BigDecimal fundingAmount;
}
