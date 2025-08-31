package com.rngad33.yxpei.model.enums.team;

import lombok.Getter;

/**
 * 队伍状态枚举
 */
@Getter
public enum TeamStatusEnum {

    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private final int value;

    private final String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

}