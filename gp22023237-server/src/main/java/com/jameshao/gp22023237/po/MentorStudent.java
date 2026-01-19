package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 双选关系表
 * @TableName mentor_student
 */
@TableName(value ="mentor_student")
@Data
public class MentorStudent {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 导师的id
     */
    private Long mentorId;

    /**
     * 学生的id
     */
    private Long studentId;

    /**
     * 导师类型（1-第一导师，2-合作导师）
     */
    private Integer mentorType;

    /**
     * 轮次：1-第一轮，2-第二轮
     */
    private Integer round;

    /**
     * 学生选择顺序（志愿）
     */
    private Integer studentChoiceOrder;

    /**
     * 学生选择状态：1-已选，0-已取消
     */
    private Integer studentStatus;

    /**
     * 导师选择状态：0-未处理，1-同意，2-拒绝
     */
    private Integer teacherStatus;

    /**
     * 学生选择时间
     */
    private Date selectionTime;

    /**
     * 导师确认时间
     */
    private Date confirmTime;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
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
        MentorStudent other = (MentorStudent) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getMentorId() == null ? other.getMentorId() == null : this.getMentorId().equals(other.getMentorId()))
            && (this.getStudentId() == null ? other.getStudentId() == null : this.getStudentId().equals(other.getStudentId()))
            && (this.getMentorType() == null ? other.getMentorType() == null : this.getMentorType().equals(other.getMentorType()))
            && (this.getRound() == null ? other.getRound() == null : this.getRound().equals(other.getRound()))
            && (this.getStudentChoiceOrder() == null ? other.getStudentChoiceOrder() == null : this.getStudentChoiceOrder().equals(other.getStudentChoiceOrder()))
            && (this.getStudentStatus() == null ? other.getStudentStatus() == null : this.getStudentStatus().equals(other.getStudentStatus()))
            && (this.getTeacherStatus() == null ? other.getTeacherStatus() == null : this.getTeacherStatus().equals(other.getTeacherStatus()))
            && (this.getSelectionTime() == null ? other.getSelectionTime() == null : this.getSelectionTime().equals(other.getSelectionTime()))
            && (this.getConfirmTime() == null ? other.getConfirmTime() == null : this.getConfirmTime().equals(other.getConfirmTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getMentorId() == null) ? 0 : getMentorId().hashCode());
        result = prime * result + ((getStudentId() == null) ? 0 : getStudentId().hashCode());
        result = prime * result + ((getMentorType() == null) ? 0 : getMentorType().hashCode());
        result = prime * result + ((getRound() == null) ? 0 : getRound().hashCode());
        result = prime * result + ((getStudentChoiceOrder() == null) ? 0 : getStudentChoiceOrder().hashCode());
        result = prime * result + ((getStudentStatus() == null) ? 0 : getStudentStatus().hashCode());
        result = prime * result + ((getTeacherStatus() == null) ? 0 : getTeacherStatus().hashCode());
        result = prime * result + ((getSelectionTime() == null) ? 0 : getSelectionTime().hashCode());
        result = prime * result + ((getConfirmTime() == null) ? 0 : getConfirmTime().hashCode());
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
        sb.append(", mentorId=").append(mentorId);
        sb.append(", studentId=").append(studentId);
        sb.append(", mentorType=").append(mentorType);
        sb.append(", round=").append(round);
        sb.append(", studentChoiceOrder=").append(studentChoiceOrder);
        sb.append(", studentStatus=").append(studentStatus);
        sb.append(", teacherStatus=").append(teacherStatus);
        sb.append(", selectionTime=").append(selectionTime);
        sb.append(", confirmTime=").append(confirmTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}