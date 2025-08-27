package com.rngad33.yxpei.model.dto.team;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 队伍管理请求体
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamManageRequest {

    private Long id;

}