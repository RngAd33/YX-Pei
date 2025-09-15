package com.rngad33.yxpei.utils;

import com.rngad33.yxpei.model.enums.misc.SpecialCharEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 特殊字符校验
 *
 * @author RngAd33
 */
public class SpecialCharValidator {

    /**
     * 强校验（防RCE、SQL注入）
     *
     * @return 是否（TF）过关
     */
    public static boolean doHighValidate(String input) {
        // 只能包含常规字符
        return containsRegularChars(input);
    }

    /**
     * 弱校验（放行部分特殊字符）
     *
     * @return 是否（TF）包含
     */
    public static boolean doLowValidate(String input) {
        // 检查是否包含攻击语句
        if (containsHackChars(input)) {
            System.out.println("---! HACKER !---");
            return true;
        }
        // 检查是否包含英文标点符号
        if (containsSpecialChars(input)) {
            System.out.println("英文标点符号！");
            return true;
        }
        // 检查是否包含空白字符
        if (containsWhiteSpaceChars(input)) {
            System.out.println("空白字符！");
            return true;
        }
        // 检查是否包含 HTML/XML 特殊字符
        if (containsHtmlSpecialChars(input)) {
            System.out.println("HTML/XML特殊字符！");
            return true;
        }
        return false;
    }

    /**
     * 检查是否全部为常规字符
     *
     * @param input
     * @return 是否（TF）过关
     */
    private static boolean containsRegularChars(String input) {
        return validateWithWhitelist(input, SpecialCharEnum.NORMAL.getValue());
    }

    /**
     * 检查是否包含英文标点符号
     *
     * @param input
     * @return 是否（TF）包含
     */
    private static boolean containsSpecialChars(String input) {
        return containsBlacklistedChars(input, SpecialCharEnum.EN_PUNCTUATION.getValue());
    }

    /**
     * 检查是否包含中文标点符号
     *
     * @param input
     * @return 是否（TF）包含
     */
    private static boolean containsChineseSpecialChars(String input) {
        return containsBlacklistedChars(input, SpecialCharEnum.CN_PUNCTUATION.getValue());
    }

    /**
     * 检查是否包含空白字符
     *
     * @param input
     * @return 是否（TF）包含
     */
    private static boolean containsWhiteSpaceChars(String input) {
        return containsBlacklistedChars(input, SpecialCharEnum.WHITESPACE.getValue());
    }

    /**
     * 检查是否包含 HTML/XML 特殊字符
     *
     * @param input
     * @return 是否（TF）包含
     */
    private static boolean containsHtmlSpecialChars(String input) {
        return containsBlacklistedChars(input, SpecialCharEnum.HTML_SPECIAL.getValue());
    }

    /**
     * 检查是否包含攻击语句
     *
     * @param input
     * @return 是否（TF）包含
     */
    private static boolean containsHackChars(String input) {
        return containsBlacklistedChars(input, SpecialCharEnum.SQL_INJECTION.getValue()) ||
                containsBlacklistedChars(input, SpecialCharEnum.DANGEROUS_COMMANDS.getValue()) ||
                containsBlacklistedChars(input, SpecialCharEnum.DANGEROUS_KEYWORDS.getValue()) ||
                containsBlacklistedChars(input, SpecialCharEnum.SHELL_METACHARACTERS.getValue()) ||
                containsBlacklistedChars(input, SpecialCharEnum.COMMAND_SEPARATORS.getValue()) ||
                containsBlacklistedChars(input, SpecialCharEnum.PATH_TRAVERSAL.getValue()) ||
                containsBlacklistedChars(input, SpecialCharEnum.DANGEROUS_PATHS.getValue()) ||
                containsBlacklistedChars(input, SpecialCharEnum.DANGEROUS_EXTENSIONS.getValue()) ||
                containsBlacklistedChars(input, SpecialCharEnum.DANGEROUS_FUNCTIONS.getValue()) ||
                containsBlacklistedChars(input, SpecialCharEnum.DANGEROUS_PROTOCOLS.getValue()) ||
                containsBlacklistedChars(input, SpecialCharEnum.ENVIRONMENT_VARIABLES.getValue());
    }

    /**
     * 黑名单校验
     *
     * @param input     待校验的输入字符串
     * @param blacklist 黑名单字符表达式
     * @return 是否（TF）包含黑名单字符
     */
    private static boolean containsBlacklistedChars(String input, String blacklist) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            // 对于包含特殊正则语法的黑名单，直接使用正则匹配
            if (blacklist.contains("\\") || blacklist.contains("[")) {
                Pattern pattern = Pattern.compile("[" + blacklist + "]");
                Matcher matcher = pattern.matcher(input);
                return matcher.find();
            } else {
                // 对于简单字符列表，使用字符串查找（更高效）
                for (char c : input.toCharArray()) {
                    if (blacklist.indexOf(c) != -1) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            // 出现异常时，使用安全的字符遍历方式
            for (char c : input.toCharArray()) {
                if (blacklist.indexOf(c) != -1) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 白名单校验
     *
     * @param input     待校验的输入字符串
     * @param whitelist 白名单字符表达式
     * @return 是否（TF）过关（只包含白名单字符）
     */
    private static boolean validateWithWhitelist(String input, String whitelist) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        try {
            // 对于包含特殊正则语法的白名单（如NORMAL），使用正则匹配
            if (whitelist.contains("\\") || whitelist.contains("[")) {
                String regex = "^[" + whitelist + "]*$";
                return input.matches(regex);
            } else {
                // 对于简单字符列表，使用字符遍历（更高效）
                for (char c : input.toCharArray()) {
                    if (whitelist.indexOf(c) == -1) {
                        return false;
                    }
                }
                return true;
            }
        } catch (Exception e) {
            // 出现异常时，使用安全的字符遍历方式
            for (char c : input.toCharArray()) {
                if (whitelist.indexOf(c) == -1) {
                    return false;
                }
            }
            return true;
        }
    }

}