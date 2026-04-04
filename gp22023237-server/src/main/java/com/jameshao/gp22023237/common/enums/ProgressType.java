package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 论文进展类型枚举
 */
@Getter
public enum ProgressType {

    PROPOSAL(1, "开题报告"),
    MIDTERM(2, "中期检查"),
    PREDEFENSE(3, "预答辩");

    private final Integer code;
    private final String desc;

    ProgressType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProgressType fromCode(Integer code) {
        for (ProgressType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
