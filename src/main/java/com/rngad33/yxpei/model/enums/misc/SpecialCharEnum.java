package com.rngad33.yxpei.model.enums.misc;

import lombok.Getter;

/**
 * 特殊字符枚举
 */
@Getter
public enum SpecialCharEnum {

    NORMAL("常规字符", "a-zA-Z0-9\\u4e00-\\u9fff\\u3400-\\u4dbf\\uf900-\\ufaff"),
    EN_PUNCTUATION("英文标点符号", "!@#$%^&*()+-=[]{}|;':\",./<>?`~\\\\pS"),
    CN_PUNCTUATION("中文标点符号", "。，、；：？！“”‘’《》【】（）！￥……——；·"),
    WHITESPACE("空白字符", " \t\n\r\f"),
    CONTROL_CHARS("控制字符","\u0000-\u001F\u007F-\u009F"),
    HTML_SPECIAL("HTML/XML特殊字符", "<>&\"'"),
    SQL_INJECTION("SQL注入相关字符","'\"\\;--");

    private final String text;

    private final String value;

    SpecialCharEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
}