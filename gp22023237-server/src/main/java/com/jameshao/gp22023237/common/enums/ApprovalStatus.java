package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 审批状态枚举
 */
@Getter
public enum ApprovalStatus {

    UNAPPROVED(0, "未审批"),
    APPROVED(1, "同意"),
    REJECTED(2, "拒绝");

    private final Integer code;
    private final String desc;

    ApprovalStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ApprovalStatus fromCode(Integer code) {
        for (ApprovalStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
