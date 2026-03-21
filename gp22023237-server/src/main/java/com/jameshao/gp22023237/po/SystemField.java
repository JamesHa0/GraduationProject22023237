package com.jameshao.gp22023237.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 系统字段管理表
 * @TableName sys_field
 */
@TableName(value ="sys_field")
@Data
public class SystemField {
    /**
     * 字段ID
     */
    @TableId(value = "field_id", type = IdType.AUTO)
    private Long fieldId;

    /**
     * 字段名称
     */
    @TableField(value = "field_name")
    private String fieldName;

    /**
     * 字段编码
     */
    @TableField(value = "field_code")
    private String fieldCode;

    /**
     * 字段类型（input输入框 select选择框 textarea文本框 date日期 datetime时间 number数字）
     */
    @TableField(value = "field_type")
    private String fieldType;

    /**
     * 字段长度
     */
    @TableField(value = "field_length")
    private Integer fieldLength;

    /**
     * 是否必填（Y是 N否）
     */
    @TableField(value = "is_required")
    private String isRequired;

    /**
     * 默认值
     */
    @TableField(value = "default_value")
    private String defaultValue;

    /**
     * 选项值（JSON格式，用于select类型）
     */
    @TableField(value = "options")
    private String options;

    /**
     * 所属模块
     */
    @TableField(value = "module")
    private String module;

    /**
     * 排序
     */
    @TableField(value = "order_num")
    private Integer orderNum;

    /**
     * 状态（0正常 1停用）
     */
    @TableField(value = "status")
    private String status;

    /**
     * 创建者
     */
    @TableField(value = "create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(value = "update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

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
        SystemField other = (SystemField) that;
        return (this.getFieldId() == null ? other.getFieldId() == null : this.getFieldId().equals(other.getFieldId()))
            && (this.getFieldName() == null ? other.getFieldName() == null : this.getFieldName().equals(other.getFieldName()))
            && (this.getFieldCode() == null ? other.getFieldCode() == null : this.getFieldCode().equals(other.getFieldCode()))
            && (this.getFieldType() == null ? other.getFieldType() == null : this.getFieldType().equals(other.getFieldType()))
            && (this.getFieldLength() == null ? other.getFieldLength() == null : this.getFieldLength().equals(other.getFieldLength()))
            && (this.getIsRequired() == null ? other.getIsRequired() == null : this.getIsRequired().equals(other.getIsRequired()))
            && (this.getDefaultValue() == null ? other.getDefaultValue() == null : this.getDefaultValue().equals(other.getDefaultValue()))
            && (this.getOptions() == null ? other.getOptions() == null : this.getOptions().equals(other.getOptions()))
            && (this.getModule() == null ? other.getModule() == null : this.getModule().equals(other.getModule()))
            && (this.getOrderNum() == null ? other.getOrderNum() == null : this.getOrderNum().equals(other.getOrderNum()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFieldId() == null) ? 0 : getFieldId().hashCode());
        result = prime * result + ((getFieldName() == null) ? 0 : getFieldName().hashCode());
        result = prime * result + ((getFieldCode() == null) ? 0 : getFieldCode().hashCode());
        result = prime * result + ((getFieldType() == null) ? 0 : getFieldType().hashCode());
        result = prime * result + ((getFieldLength() == null) ? 0 : getFieldLength().hashCode());
        result = prime * result + ((getIsRequired() == null) ? 0 : getIsRequired().hashCode());
        result = prime * result + ((getDefaultValue() == null) ? 0 : getDefaultValue().hashCode());
        result = prime * result + ((getOptions() == null) ? 0 : getOptions().hashCode());
        result = prime * result + ((getModule() == null) ? 0 : getModule().hashCode());
        result = prime * result + ((getOrderNum() == null) ? 0 : getOrderNum().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", fieldId=").append(fieldId);
        sb.append(", fieldName=").append(fieldName);
        sb.append(", fieldCode=").append(fieldCode);
        sb.append(", fieldType=").append(fieldType);
        sb.append(", fieldLength=").append(fieldLength);
        sb.append(", isRequired=").append(isRequired);
        sb.append(", defaultValue=").append(defaultValue);
        sb.append(", options=").append(options);
        sb.append(", module=").append(module);
        sb.append(", orderNum=").append(orderNum);
        sb.append(", status=").append(status);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", remark=").append(remark);
        sb.append("]");
        return sb.toString();
    }
}
