package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.util.Date;

/**
 * 统一审核列表项DTO
 */
@Data
public class ReviewItemDTO {
    private Long id;
    private Integer type; // 1-学术活动, 2-创新项目, 3-学术成果
    private String title;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Date submitTime;
    private Date createTime;
    private String description;

    // 三级审批状态
    private Integer mentorStatus;
    private String mentorComment;
    private Integer secretaryStatus;
    private String secretaryComment;
    private Integer deanStatus;
    private String deanComment;

    // 统一显示的状态（根据当前用户角色计算）
    private Integer status; // 0-待审批, 1-已通过, 2-已拒绝
    private String approvalComment;

    // 额外的详情字段
    private String extraInfo;
}
