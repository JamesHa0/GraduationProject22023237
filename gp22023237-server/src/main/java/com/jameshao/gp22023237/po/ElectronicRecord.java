package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学生电子档案表
 * @TableName electronic_record
 */
@TableName(value = "electronic_record")
@Data
public class ElectronicRecord {
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
     * 档案类型：1-基本信息，2-学习经历，3-奖惩情况，4-科研成果，5-其他
     */
    private Integer type;

    /**
     * 档案内容
     */
    private String content;

    /**
     * 附件URL
     */
    private String fileUrl;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
