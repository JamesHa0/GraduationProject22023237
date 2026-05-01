package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 论文流程环节类型枚举
 */
@Getter
public enum ProcessType {

    PROPOSAL(1, "开题报告"),
    MIDTERM(2, "中期检查"),
    PRE_DEFENSE(3, "预答辩"),
    EXTERNAL_REVIEW(4, "论文外审"),
    DEFENSE(5, "正式答辩"),
    SECOND_DEFENSE(6, "二次答辩"),
    RE_SUBMISSION(7, "修改后再审");

    private final Integer code;
    private final String desc;

    ProcessType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProcessType fromCode(Integer code) {
        for (ProcessType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
