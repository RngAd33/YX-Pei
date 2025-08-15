package com.rngad33.yxpei.utils;

import com.rngad33.yxpei.exception.MyException;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;

/**
 * 异常抛出工具类
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param myException 异常
     */
    public static void throwIf(boolean condition, MyException myException) {
        if (condition) {
            throw myException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, ErrorCodeEnum errorCode) {
        throwIf(condition, new MyException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static void throwIf(boolean condition, ErrorCodeEnum errorCode, String message) {
        throwIf(condition, new MyException(errorCode, message));
    }

}