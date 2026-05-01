package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 审批动作枚举
 */
@Getter
public enum ApprovalAction {

    SUBMIT(1, "提交"),
    APPROVE(2, "通过"),
    REJECT(3, "驳回"),
    WITHDRAW(4, "撤回");

    private final Integer code;
    private final String desc;

    ApprovalAction(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ApprovalAction fromCode(Integer code) {
        for (ApprovalAction action : values()) {
            if (action.getCode().equals(code)) {
                return action;
            }
        }
        return null;
    }
}
