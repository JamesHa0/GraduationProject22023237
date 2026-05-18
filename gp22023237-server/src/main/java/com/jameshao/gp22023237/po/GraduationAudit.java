package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.util.Date;

/**
 * 毕业资格审核实体类
 * 数据库表: graduation_qualification
 * 字段名与数据库表结构保持一致，通过@TableField映射
 */
@TableName(value = "graduation_qualification")
@Data
public class GraduationAudit {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生ID - 关联学生表 */
    private Long studentId;

    /** 学分检查：0-未审核，1-通过，2-未通过 */
    @TableField("credit_check")
    private Integer creditsPass;

    /** 论文检查：0-未审核，1-通过，2-未通过 */
    @TableField("thesis_check")
    private Integer thesisPass;

    /** 实践检查：0-未审核，1-通过，2-未通过 */
    @TableField("practice_check")
    private Integer practicePass;

    /** 总结果：0-待审核，1-通过，2-不通过 */
    @TableField("overall_result")
    private Integer auditStatus;

    /** 审核人ID */
    @TableField("reviewer_id")
    private Long auditorId;

    /** 审核时间 */
    @TableField("review_time")
    private Date auditTime;

    /** 审核意见 */
    private String comment;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
