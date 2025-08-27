package com.rngad33.yxpei.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 队伍模型
 */
@Data
@Table("team")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Team {

    /**
     * 队伍id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 人数上限
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人id（队长）
     */
    private Long leaderId;

    /**
     * 是否需要队长审批？0-不需要，1-需要
     */
    private Integer needApproval;

    /**
     * 开放状态？0-公开，1-私有，2-加密
     */
    private Integer status;

    /**
     * 加入密码
     */
    private String teamPassword;

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