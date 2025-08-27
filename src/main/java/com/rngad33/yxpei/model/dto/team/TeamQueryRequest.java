package com.rngad33.yxpei.model.dto.team;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rngad33.yxpei.model.entity.User;
import lombok.Data;

/**
 * 队伍查询请求体
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamQueryRequest {

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 创建人（队长）
     */
    private User leader;

}