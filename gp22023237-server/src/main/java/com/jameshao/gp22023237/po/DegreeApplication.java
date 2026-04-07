package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学位申请表
 * @TableName degree_application
 */
@TableName(value = "degree_application")
@Data
public class DegreeApplication {
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
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 导师ID
     */
    private Long mentorId;

    /**
     * 导师姓名
     */
    private String mentorName;

    /**
     * 申请学位类型：1-硕士学位，2-博士学位
     */
    private Integer degreeType;

    /**
     * 论文题目
     */
    private String thesisTitle;

    /**
     * 论文最终稿附件路径
     */
    private String thesisAttachment;

    /**
     * 答辩时间
     */
    private Date defenseTime;

    /**
     * 答辩地点
     */
    private String defenseLocation;

    /**
     * 答辩委员会主席
     */
    private String committeeChair;

    /**
     * 答辩委员会成员
     */
    private String committeeMembers;

    /**
     * 答辩结果：0-未答辩，1-通过，2-不通过
     */
    private Integer defenseResult;

    /**
     * 答辩评分（百分制）
     */
    private Double defenseScore;

    /**
     * 答委评语
     */
    private String defenseCommitteeComment;

    /**
     * 学位分委员会审批状态：0-未审批，1-同意，2-拒绝
     */
    private Integer committeeStatus;

    /**
     * 学位分委员会审批意见
     */
    private String committeeComment;

    /**
     * 学位授予状态：0-未授予，1-已授予
     */
    private Integer degreeGranted;

    /**
     * 学位授予日期
     */
    private Date degreeGrantDate;

    /**
     * 学位证书编号
     */
    private String certificateNo;

    /**
     * 附件路径（申请材料）
     */
    private String attachmentPath;

    /**
     * 答辩问答记录
     */
    private String qaRecord;

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
