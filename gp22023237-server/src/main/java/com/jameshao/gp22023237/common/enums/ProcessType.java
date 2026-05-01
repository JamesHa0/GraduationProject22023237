package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 论文流程环节类型枚举（VGMS风格7环节）
 */
@Getter
public enum ProcessType {

    TOPIC(1, "选题"),
    TASK_BOOK(2, "任务书"),
    PROPOSAL(3, "开题报告"),
    MIDTERM(4, "中期检查"),
    DRAFT(5, "过程稿"),
    DEFENSE_DRAFT(6, "论文答辩稿"),
    FINAL_THESIS(7, "毕业论文");

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
