package com.rngad33.yxpei.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mybatisflex.annotation.*;
import com.mybatisflex.core.mask.Masks;
import lombok.Data;

import java.util.Date;

/**
 * 用户模型
 */
@Data
@Table("user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    /**
     * 用户 id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 身份？ 0-普通用户，1-管理员
     */
    private Integer role;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 性别：0-女，1-男
     */
    private Integer gender;

    /**
     * 密码
     */
    @ColumnMask(Masks.PASSWORD)
    private String userPassword;

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
     * 标签列表（JSON）
     */
    private String tags;

    /**
     * 用户状态：0-正常，1-封禁
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除？ 0-未删，1-已删
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;

}