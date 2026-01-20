package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 教师信息表
 * @TableName teacher
 */
@TableName(value ="teacher")
@Data
public class Teacher {
    /**
     * 教师ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 教师编号
     */
    private String teacherNo;

    /**
     * 职称
     */
    private String title;

    /**
     * 院系
     */
    private String department;

    /**
     * 研究领域（仅导师有值）
     */
    private String researchField;

    /**
     * 招生名额（仅导师有值）
     */
    private Integer quota;

    /**
     * 剩余名额（仅导师有值）
     */
    private Integer remainingQuota;

    /**
     * 已确认名额（仅导师有值）
     */
    private Integer confirmedQuota;

    /**
     * 是否可指导研究生：0-否，1-是
     */
    private Integer isMentor;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
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
        Teacher other = (Teacher) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTeacherName() == null ? other.getTeacherName() == null : this.getTeacherName().equals(other.getTeacherName()))
            && (this.getTeacherNo() == null ? other.getTeacherNo() == null : this.getTeacherNo().equals(other.getTeacherNo()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getDepartment() == null ? other.getDepartment() == null : this.getDepartment().equals(other.getDepartment()))
            && (this.getResearchField() == null ? other.getResearchField() == null : this.getResearchField().equals(other.getResearchField()))
            && (this.getQuota() == null ? other.getQuota() == null : this.getQuota().equals(other.getQuota()))
            && (this.getRemainingQuota() == null ? other.getRemainingQuota() == null : this.getRemainingQuota().equals(other.getRemainingQuota()))
            && (this.getConfirmedQuota() == null ? other.getConfirmedQuota() == null : this.getConfirmedQuota().equals(other.getConfirmedQuota()))
            && (this.getIsMentor() == null ? other.getIsMentor() == null : this.getIsMentor().equals(other.getIsMentor()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTeacherName() == null) ? 0 : getTeacherName().hashCode());
        result = prime * result + ((getTeacherNo() == null) ? 0 : getTeacherNo().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getDepartment() == null) ? 0 : getDepartment().hashCode());
        result = prime * result + ((getResearchField() == null) ? 0 : getResearchField().hashCode());
        result = prime * result + ((getQuota() == null) ? 0 : getQuota().hashCode());
        result = prime * result + ((getRemainingQuota() == null) ? 0 : getRemainingQuota().hashCode());
        result = prime * result + ((getConfirmedQuota() == null) ? 0 : getConfirmedQuota().hashCode());
        result = prime * result + ((getIsMentor() == null) ? 0 : getIsMentor().hashCode());
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
        sb.append(", teacherName=").append(teacherName);
        sb.append(", teacherNo=").append(teacherNo);
        sb.append(", title=").append(title);
        sb.append(", department=").append(department);
        sb.append(", researchField=").append(researchField);
        sb.append(", quota=").append(quota);
        sb.append(", remainingQuota=").append(remainingQuota);
        sb.append(", confirmedQuota=").append(confirmedQuota);
        sb.append(", isMentor=").append(isMentor);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}