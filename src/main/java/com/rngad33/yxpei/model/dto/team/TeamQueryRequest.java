package com.rngad33.yxpei.model.dto.team;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rngad33.yxpei.common.PageRequest;
import com.rngad33.yxpei.model.entity.User;
import lombok.Data;

/**
 * 队伍查询请求体
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamQueryRequest extends PageRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 创建人id（队长）
     */
    private Long leaderId;

    /**
     * 开放状态？0-公开，1-私有，2-加密
     */
    private Integer status;

}