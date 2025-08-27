package com.rngad33.yxpei.service;

import com.mybatisflex.core.service.IService;
import com.rngad33.yxpei.model.dto.team.TeamCreateRequest;
import com.rngad33.yxpei.model.dto.team.TeamEditRequest;
import com.rngad33.yxpei.model.entity.Team;
import com.rngad33.yxpei.model.entity.User;

/**
 * 队伍服务接口
 */
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param request
     * @param loginUser
     * @return
     */
    Long teamCreate(TeamCreateRequest request, User loginUser) throws Exception;

    /**
     * 编辑队伍
     *
     * @param request
     * @param loginUser
     * @return
     */
    Integer teamEdit(TeamEditRequest request, User loginUser);

}