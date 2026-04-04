package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 学位类型枚举
 */
@Getter
public enum DegreeType {

    MASTER(1, "硕士学位"),
    DOCTOR(2, "博士学位");

    private final Integer code;
    private final String desc;

    DegreeType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DegreeType fromCode(Integer code) {
        for (DegreeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
