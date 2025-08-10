package com.rngad33.yxpei.model.vo;

import com.mybatisflex.annotation.ColumnMask;
import com.mybatisflex.core.mask.Masks;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 用户视图（脱敏）
 */
@Data
public class UserVO {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 性别：0-女，1-男
     */
    private Integer gender;

    /**
     * 电话
     */
    @ColumnMask(Masks.FIXED_PHONE)
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 用户状态：0-正常，1-封禁
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

}