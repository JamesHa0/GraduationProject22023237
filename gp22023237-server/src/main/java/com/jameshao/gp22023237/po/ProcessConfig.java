package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 流程环节配置表
 * @TableName thesis_process_config
 */
@TableName(value = "thesis_process_config")
@Data
public class ProcessConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流程环节类型：1-开题，2-中期，3-预答辩，4-外审，5-正式答辩，6-二次答辩，7-修改后再审 */
    private Integer processType;

    /** 环节名称 */
    private String processName;

    /** 是否启用：0-禁用，1-启用 */
    private Integer enabled;

    /** 截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date deadline;

    /** 是否需要导师审批：0-否，1-是 */
    private Integer needSupervisorApproval;

    /** 是否需要秘书审批：0-否，1-是 */
    private Integer needSecretaryApproval;

    /** 是否需要院长审批：0-否，1-是 */
    private Integer needDeanApproval;

    /** 是否需要录入评审结果：0-否，1-是 */
    private Integer needReviewResult;

    /** 排序号 */
    private Integer sort;

    /** 备注 */
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
