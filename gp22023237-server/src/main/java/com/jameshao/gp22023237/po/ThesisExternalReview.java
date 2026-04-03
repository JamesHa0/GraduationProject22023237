package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 论文外审表
 * @TableName thesis_external_review
 */
@TableName(value = "thesis_external_review")
@Data
public class ThesisExternalReview {
    /**
     * 外审ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 论文链接
     */
    private String thesisUrl;

    /**
     * 外审专家
     */
    private String reviewers;

    /**
     * 外审时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewTime;

    /**
     * 外审结果：0-未完成，1-通过，2-修改后通过，3-不通过
     */
    private Integer reviewResult;

    /**
     * 外审意见
     */
    private String reviewComments;

    /**
     * 状态：0-未提交，1-外审中，2-已完成
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
