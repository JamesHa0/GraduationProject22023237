package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 创新实践项目带学生详情DTO
 */
@Data
public class InnovationProjectWithDetailsDTO {
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Integer projectType;
    private String projectName;
    private Integer projectLevel;
    private String projectNo;
    private String leader;
    private String members;
    private String advisor;
    private Date startDate;
    private Date endDate;
    private String description;
    private String achievements;
    private Integer awardLevel;
    private BigDecimal fundingAmount;
    private Integer mentorStatus;
    private String mentorComment;
    private Integer secretaryStatus;
    private String secretaryComment;
    private Integer deanStatus;
    private String deanComment;
    private String attachmentPath;
    private Date submitTime;
    private Date createTime;
    private Date updateTime;

    // 兼容前端字段
    public Integer getType() {
        return projectType;
    }

    public String getTitle() {
        return projectName;
    }

    public Date getTime() {
        return startDate;
    }

    public String getAward() {
        return achievements;
    }

    public String getOrganizer() {
        return advisor;
    }

    public Integer getTutorApproval() {
        return mentorStatus;
    }
}
