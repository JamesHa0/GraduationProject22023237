package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 提交人类型枚举
 */
@Getter
public enum SubmitterType {

    STUDENT(1, "学生"),
    MENTOR(2, "导师"),
    ADMIN(3, "管理员");

    private final Integer code;
    private final String desc;

    SubmitterType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SubmitterType fromCode(Integer code) {
        for (SubmitterType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
