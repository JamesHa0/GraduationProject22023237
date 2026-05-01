package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 论文主表
 * 一个学生对应一篇毕业论文
 * @TableName thesis_main
 */
@TableName(value = "thesis_main")
@Data
public class ThesisMain {
    /**
     * 论文主ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号（冗余，便于查询展示）
     */
    private String studentNo;

    /**
     * 学生姓名（冗余，便于查询展示）
     */
    private String studentName;

    /**
     * 导师ID
     */
    private Long supervisorId;

    /**
     * 导师姓名（冗余，便于查询展示）
     */
    private String supervisorName;

    /**
     * 专业
     */
    private String major;

    /**
     * 年级
     */
    private String grade;

    /**
     * 最终论文题目
     */
    private String thesisTitle;

    /**
     * 最终定稿论文路径
     */
    private String thesisFinalUrl;

    /**
     * 论文最终结果：0-进行中，1-通过，2-未通过
     */
    private Integer finalResult;

    /**
     * 最终答辩评分
     */
    private BigDecimal finalScore;

    /**
     * 归档状态：0-未归档，1-已归档
     */
    private Integer archiveStatus;

    /**
     * 归档时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date archiveTime;

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
