package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 论文流程环节状态枚举
 */
@Getter
public enum ProcessStatus {

    NOT_SUBMITTED(0, "未提交"),
    APPROVING(1, "审批中"),
    REVIEWING(2, "评审中"),
    PASSED(3, "已通过"),
    REJECTED(4, "已拒绝"),
    COMPLETED(5, "已完成");

    private final Integer code;
    private final String desc;

    ProcessStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProcessStatus fromCode(Integer code) {
        for (ProcessStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查是否允许状态流转
     */
    public static boolean canTransition(ProcessStatus current, ProcessStatus target) {
        if (current == null || target == null) {
            return false;
        }
        switch (current) {
            case NOT_SUBMITTED:
                return target == APPROVING;
            case APPROVING:
                return target == REVIEWING || target == REJECTED || target == PASSED;
            case REVIEWING:
                return target == PASSED || target == COMPLETED || target == REJECTED;
            case PASSED:
                return target == COMPLETED;
            case REJECTED:
                return target == APPROVING; // 允许重新提交
            case COMPLETED:
                return false;
            default:
                return false;
        }
    }
}
