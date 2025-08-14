package com.rngad33.yxpei.once;

import com.alibaba.excel.annotation.ExcelProperty;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表格用户信息
 */
@Data
@EqualsAndHashCode
public class TableUserInfo {

    /**
     * 用户 id
     */
    @ExcelProperty("id")
    private Long id;

    /**
     * 用户昵称
     */
    @ExcelProperty("用户昵称")
    private String userName;

}