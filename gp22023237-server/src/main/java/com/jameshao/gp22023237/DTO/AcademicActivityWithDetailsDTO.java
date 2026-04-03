package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.util.Date;

/**
 * 学术活动带学生详情DTO
 */
@Data
public class AcademicActivityWithDetailsDTO {
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Integer activityType;
    private String activityName;
    private Date activityTime;
    private String location;
    private String speaker;
    private String content;
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
        return activityType;
    }

    public String getTitle() {
        return activityName;
    }

    public Date getTime() {
        return activityTime;
    }

    public String getDescription() {
        return content;
    }

    public String getOrganizer() {
        return speaker;
    }

    public Integer getTutorApproval() {
        return mentorStatus;
    }
}
