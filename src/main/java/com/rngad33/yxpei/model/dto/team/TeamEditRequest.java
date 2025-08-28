package com.rngad33.yxpei.model.dto.team;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rngad33.yxpei.model.entity.User;
import lombok.Data;

import java.util.Date;

/**
 * 队伍编辑请求体
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamEditRequest {

    /**
     * 队伍id
     */
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
    private User leaderId;

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

}