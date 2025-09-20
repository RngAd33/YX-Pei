package com.rngad33.yxpei.model.dto.team;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 队伍加入请求体
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamJoinRequest {

    /**
     * 目标队伍id
     */
    private Long teamId;

    /**
     * 密码（如果被加密）
     */
    private String password;

}