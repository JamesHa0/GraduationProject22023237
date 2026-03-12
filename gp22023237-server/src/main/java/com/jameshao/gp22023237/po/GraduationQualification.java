package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 毕业资格审核表
 * @TableName graduation_qualification
 */
@TableName(value = "graduation_qualification")
@Data
public class GraduationQualification {
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
     * 学分审核：0-未通过，1-通过
     */
    private Integer creditCheck;

    /**
     * 论文审核：0-未通过，1-通过
     */
    private Integer thesisCheck;

    /**
     * 实践环节审核：0-未通过，1-通过
     */
    private Integer practiceCheck;

    /**
     * 综合审核结果：0-未通过，1-通过
     */
    private Integer overallResult;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 审核意见
     */
    private String comment;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
