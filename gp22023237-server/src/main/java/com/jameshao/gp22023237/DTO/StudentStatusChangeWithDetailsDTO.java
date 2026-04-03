package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.util.Date;

/**
 * 学籍异动申请带学生详情DTO
 */
@Data
public class StudentStatusChangeWithDetailsDTO {
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Integer changeType;
    private String reason;
    private Date startDate;
    private Date endDate;
    private Integer mentorStatus;
    private Long mentorId;
    private Date mentorApprovalTime;
    private Integer secretaryStatus;
    private Long secretaryId;
    private Date secretaryApprovalTime;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    // 用于前端展示的兼容字段
    public Date getEffectiveDate() {
        return startDate;
    }

    public Date getApplyTime() {
        return createTime;
    }

    // 前端需要deanStatus，但数据库没有，默认返回null或0
    public Integer getDeanStatus() {
        return null;
    }
}
