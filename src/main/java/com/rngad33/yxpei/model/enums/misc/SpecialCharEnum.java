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

    SQL_INJECTION("SQL注入相关字符","'\"\\;--"),
    DANGEROUS_COMMANDS("危险命令", "rm|shutdown|reboot|kill|halt|init|poweroff|crash|exec|system|eval|assert|passthru|shell_exec"),
    DANGEROUS_KEYWORDS("危险关键字", "exec|system|eval|assert|passthru|shell_exec|popen|proc_open|pcntl_exec|assert|create_function"),
    SHELL_METACHARACTERS("Shell元字符", "|&;<>`\\\"'(){}[]*$?@#~=%!^"),
    COMMAND_SEPARATORS("命令分隔符", "|&;<>`\\\"'(){}[]*$?#~=@%!^\\r\\n\\t"),
    PATH_TRAVERSAL("路径遍历字符", "../|..\\\\|./|..\\|~|/etc/passwd"),
    DANGEROUS_PATHS("危险路径", "/etc/|/bin/|/usr/bin|/usr/local/bin|C:\\\\Windows\\\\|C:\\\\Program"),
    DANGEROUS_EXTENSIONS("危险文件扩展名", "\\.sh|\\.bat|\\.exe|\\.cmd|\\.ps1|\\.pl|\\.py|\\.rb|\\.php"),
    DANGEROUS_FUNCTIONS("危险函数", "exec|system|eval|assert|passthru|shell_exec|popen|proc_open|pcntl_exec|create_function|call_user_func|call_user_func_array"),
    DANGEROUS_PROTOCOLS("危险协议", "file://|php://|zlib://|data://|glob://|phar://|ssh2://|rar://|ogg://|expect://"),
    ENVIRONMENT_VARIABLES("环境变量", "\\$\\{|\\$\\w+|%\\w+%|\\$PATH|\\$HOME|\\$USER");

    private final String text;

    private final String value;

    SpecialCharEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
}