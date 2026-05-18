package com.jameshao.gp22023237.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 毕业资格审核带学生详情DTO
 * 用于前端列表展示，包含学生姓名/学号/总学分等关联查询字段
 */
@Data
public class GraduationAuditWithDetailsDTO {
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String department;
    private String major;
    private Integer admissionYear;

    /** 学分审核：0-未审核，1-通过，2-未通过 */
    private Integer creditCheck;
    /** 论文审核：0-未审核，1-通过，2-未通过 */
    private Integer thesisCheck;
    /** 实践审核：0-未审核，1-通过，2-未通过 */
    private Integer practiceCheck;
    /** 总结果：0-待审核，1-通过，2-不通过 */
    private Integer auditStatus;

    /** 已修总学分（从score+course汇总） */
    private BigDecimal totalCredits;
    /** 毕业要求学分（从系统配置获取） */
    private BigDecimal requiredCredits;

    private Long auditorId;
    private String auditorName;
    private Date auditTime;
    private String comment;
    private Date createTime;
    private Date updateTime;
}
