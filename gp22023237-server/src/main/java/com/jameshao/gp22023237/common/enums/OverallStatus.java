package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 综合状态枚举
 */
@Getter
public enum OverallStatus {

    UNSUBMITTED(0, "未提交"),
    MENTOR_APPROVING(1, "导师审批中"),
    SECRETARY_APPROVING(2, "秘书审批中"),
    DEAN_APPROVING(3, "院长审批中"),
    PASSED(4, "已通过"),
    REJECTED(5, "已拒绝");

    private final Integer code;
    private final String desc;

    OverallStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OverallStatus fromCode(Integer code) {
        for (OverallStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查是否允许状态流转
     * @param currentStatus 当前状态
     * @param targetStatus 目标状态
     * @return 是否允许流转
     */
    public static boolean canTransition(OverallStatus currentStatus, OverallStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return false;
        }

        switch (currentStatus) {
            case UNSUBMITTED:
                return targetStatus == MENTOR_APPROVING;
            case MENTOR_APPROVING:
                return targetStatus == SECRETARY_APPROVING || targetStatus == REJECTED;
            case SECRETARY_APPROVING:
                return targetStatus == DEAN_APPROVING || targetStatus == REJECTED;
            case DEAN_APPROVING:
                return targetStatus == PASSED || targetStatus == REJECTED;
            case PASSED:
            case REJECTED:
                return false;
            default:
                return false;
        }
    }
}
