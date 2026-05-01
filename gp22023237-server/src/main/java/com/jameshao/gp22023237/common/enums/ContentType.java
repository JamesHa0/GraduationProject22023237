package com.jameshao.gp22023237.common.enums;

import lombok.Getter;

/**
 * 学术内容类型枚举
 */
@Getter
public enum ContentType {

    ACTIVITY(1, "学术活动"),
    ACHIEVEMENT(2, "学术成果"),
    INNOVATION(3, "创新创业");

    private final Integer code;
    private final String desc;

    ContentType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ContentType fromCode(Integer code) {
        for (ContentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
