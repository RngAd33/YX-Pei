package com.rngad33.yxpei.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.rngad33.yxpei.model.dto.team.*;
import com.rngad33.yxpei.model.entity.Team;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.vo.TeamVO;

import java.util.List;

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
    Long teamCreate(TeamCreateRequest request, User loginUser);

    /**
     * 编辑队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    Integer teamEdit(Team team, User loginUser) throws Exception;

    /**
     * 查询队伍列表
     *
     * @param teamQueryRequest
     * @return
     */
    List<TeamVO> listTeams(TeamQueryRequest teamQueryRequest, boolean isAdmin);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    Boolean teamJoin(TeamJoinRequest teamJoinRequest, User loginUser) throws Exception ;

    /**
     * 退出队伍
     *
     * @param teamExitRequest
     * @param loginUser
     * @return
     */
    Boolean teamExit(TeamExitRequest teamExitRequest, User loginUser);

    /**
     * 分页查询对象构建
     *
     * @param teamQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(TeamQueryRequest teamQueryRequest);

}