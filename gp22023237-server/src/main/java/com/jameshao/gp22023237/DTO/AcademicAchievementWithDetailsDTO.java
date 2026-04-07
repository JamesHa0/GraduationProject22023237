package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.util.Date;

/**
 * 学术成果带学生详情DTO
 */
@Data
public class AcademicAchievementWithDetailsDTO {
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Integer achievementType;
    private String title;
    private String authors;
    private Date publicationDate;
    private String journalName;
    private Integer journalLevel;
    private String volume;
    private String issue;
    private String pages;
    private String doi;
    private String patentNo;
    private Integer patentType;
    private Integer patentStatus;
    private String awardName;
    private Integer awardLevel;
    private String awardIssuer;
    private String projectName;
    private Integer projectRole;
    private String abstractContent;
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
        return achievementType;
    }

    public Date getPublicationTime() {
        return publicationDate;
    }

    public String getDescription() {
        return abstractContent;
    }

    public String getFileUrl() {
        return attachmentPath;
    }

    public Integer getTutorApproval() {
        return mentorStatus;
    }

    public Integer getSecretaryApproval() {
        return secretaryStatus;
    }
}
