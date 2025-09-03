package com.rngad33.yxpei.model.dto.team;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 队伍退出请求体
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamExitRequest {

    /**
     * 目标队伍id
     */
    private Long teamId;

}