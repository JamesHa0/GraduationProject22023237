package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 学位相关状态枚举
 * 统一答辩结果、分委审批、学位授予等状态的code与描述映射
 * 数据库值保持不变，仅枚举描述层面统一
 */
@Getter
public enum DegreeStatusEnum {

    // 答辩结果（degree_application.defense_result）
    DEFENSE_PENDING(0, "待答辩"),
    DEFENSE_PASSED(1, "答辩通过"),
    DEFENSE_FAILED(2, "答辩不通过"),

    // 分委审批（degree_application.committee_status）
    COMMITTEE_PENDING(0, "待审批"),
    COMMITTEE_APPROVED(1, "审批通过"),
    COMMITTEE_REJECTED(2, "审批拒绝"),

    // 学位授予（degree_application.degree_granted）
    DEGREE_NOT_GRANTED(0, "未授予"),
    DEGREE_GRANTED(1, "已授予");

    private final Integer code;
    private final String desc;

    DegreeStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DegreeStatusEnum fromCode(Integer code) {
        for (DegreeStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 获取答辩结果描述
     */
    public static String getDefenseDesc(Integer code) {
        if (code == null) return DEFENSE_PENDING.getDesc();
        switch (code) {
            case 1: return DEFENSE_PASSED.getDesc();
            case 2: return DEFENSE_FAILED.getDesc();
            default: return DEFENSE_PENDING.getDesc();
        }
    }

    /**
     * 获取分委审批描述
     */
    public static String getCommitteeDesc(Integer code) {
        if (code == null) return COMMITTEE_PENDING.getDesc();
        switch (code) {
            case 1: return COMMITTEE_APPROVED.getDesc();
            case 2: return COMMITTEE_REJECTED.getDesc();
            default: return COMMITTEE_PENDING.getDesc();
        }
    }

    /**
     * 获取学位授予描述
     */
    public static String getDegreeDesc(Integer code) {
        if (code == null || code == 0) return DEGREE_NOT_GRANTED.getDesc();
        return DEGREE_GRANTED.getDesc();
    }
}
