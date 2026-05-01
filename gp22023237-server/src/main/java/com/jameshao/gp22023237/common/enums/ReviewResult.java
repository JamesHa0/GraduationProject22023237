package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 评审/答辩结果枚举
 */
@Getter
public enum ReviewResult {

    NOT_STARTED(0, "未进行"),
    PASSED(1, "通过"),
    MODIFY_PASSED(2, "修改后通过"),
    FAILED(3, "未通过");

    private final Integer code;
    private final String desc;

    ReviewResult(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReviewResult fromCode(Integer code) {
        for (ReviewResult result : values()) {
            if (result.getCode().equals(code)) {
                return result;
            }
        }
        return null;
    }
}
