package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 学生信息实体类
 * 用于存储研究生的基本学籍信息，包括学号、姓名、院系、专业、入学毕业年份、研究方向等
 * 同时记录学生的导师双选状态和学籍状态
 * @TableName student
 */
@TableName(value ="student")
@Data
public class Student {
    /**
     * 学生ID - 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联系统用户ID - 用于登录认证和权限管理
     */
    private Long userId;

    /**
     * 学号 - 学生唯一标识，如：2023001
     */
    private String studentNo;

    /**
     * 学生姓名 - 学生的真实姓名
     */
    private String studentName;

    /**
     * 院系 - 学生所属学院/系，如：计算机学院
     */
    private String department;

    /**
     * 专业 - 学生就读的专业，如：计算机科学与技术
     */
    private String major;

    /**
     * 入学年份 - 整数类型，如：2023
     */
    private Integer admissionYear;

    /**
     * 预计毕业年份 - 整数类型，如：2026
     */
    private Integer graduationYear;

    /**
     * 研究方向 - 学生的具体研究领域，如：人工智能、软件工程等
     */
    private String researchDirection;

    /**
     * 导师双选状态 - 0:未开始 1:第一轮双选 2:第二轮双选 3:已确定导师
     */
    private Integer selectionStatus;

    /**
     * 学籍状态 - 1:在读 2:休学 3:毕业 4:退学
     */
    private Integer status;

    /**
     * 创建时间 - 记录学生信息创建的时间
     */
    private Date createTime;

    /**
     * 更新时间 - 记录学生信息最后更新的时间
     */
    private Date updateTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Student other = (Student) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getStudentNo() == null ? other.getStudentNo() == null : this.getStudentNo().equals(other.getStudentNo()))
            && (this.getStudentName() == null ? other.getStudentName() == null : this.getStudentName().equals(other.getStudentName()))
            && (this.getDepartment() == null ? other.getDepartment() == null : this.getDepartment().equals(other.getDepartment()))
            && (this.getMajor() == null ? other.getMajor() == null : this.getMajor().equals(other.getMajor()))
            && (this.getAdmissionYear() == null ? other.getAdmissionYear() == null : this.getAdmissionYear().equals(other.getAdmissionYear()))
            && (this.getGraduationYear() == null ? other.getGraduationYear() == null : this.getGraduationYear().equals(other.getGraduationYear()))
            && (this.getResearchDirection() == null ? other.getResearchDirection() == null : this.getResearchDirection().equals(other.getResearchDirection()))
            && (this.getSelectionStatus() == null ? other.getSelectionStatus() == null : this.getSelectionStatus().equals(other.getSelectionStatus()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getStudentNo() == null) ? 0 : getStudentNo().hashCode());
        result = prime * result + ((getStudentName() == null) ? 0 : getStudentName().hashCode());
        result = prime * result + ((getDepartment() == null) ? 0 : getDepartment().hashCode());
        result = prime * result + ((getMajor() == null) ? 0 : getMajor().hashCode());
        result = prime * result + ((getAdmissionYear() == null) ? 0 : getAdmissionYear().hashCode());
        result = prime * result + ((getGraduationYear() == null) ? 0 : getGraduationYear().hashCode());
        result = prime * result + ((getResearchDirection() == null) ? 0 : getResearchDirection().hashCode());
        result = prime * result + ((getSelectionStatus() == null) ? 0 : getSelectionStatus().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", studentNo=").append(studentNo);
        sb.append(", studentName=").append(studentName);
        sb.append(", department=").append(department);
        sb.append(", major=").append(major);
        sb.append(", admissionYear=").append(admissionYear);
        sb.append(", graduationYear=").append(graduationYear);
        sb.append(", researchDirection=").append(researchDirection);
        sb.append(", selectionStatus=").append(selectionStatus);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}