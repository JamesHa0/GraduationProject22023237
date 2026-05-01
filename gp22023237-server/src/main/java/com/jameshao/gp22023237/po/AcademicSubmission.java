package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.util.Date;

/**
 * 学术内容提交主表
 * @TableName academic_submission
 */
@TableName(value = "academic_submission")
@Data
public class AcademicSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 归属学生ID（关联student表） */
    private Long studentId;

    /** 提交人ID（学生=student.id，导师=teacher.id，管理员=user.id） */
    private Long submitterId;

    /** 提交人类型：1=学生，2=导师，3=管理员 */
    private Integer submitterType;

    /** 内容类型：1=学术活动，2=学术成果，3=创新创业 */
    private Integer contentType;

    /** 内容标题 */
    private String title;

    /** 内容摘要/说明 */
    private String abstractContent;

    /** 附件文件URL列表（JSON数组） */
    private String fileUrls;

    /** 审批状态：0=待提交，1=待导师审批，2=待秘书审批，3=待院长审批，4=已通过，5=已驳回 */
    private Integer approvalStatus;

    /** 当前审批人ID */
    private Long currentApproverId;

    /** 当前审批人类型：2=导师，5=教学秘书，1=分管院长 */
    private Integer currentApproverType;

    /** 提交时间 */
    private Date submitTime;

    /** 软删除：0=未删除，1=已删除 */
    private Integer isDeleted;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    private Date createTime;

    private Date updateTime;
}
