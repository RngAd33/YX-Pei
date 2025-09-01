package com.rngad33.yxpei.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.rngad33.yxpei.model.dto.team.TeamCreateRequest;
import com.rngad33.yxpei.model.dto.team.TeamEditRequest;
import com.rngad33.yxpei.model.dto.team.TeamQueryRequest;
import com.rngad33.yxpei.model.entity.Team;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.vo.TeamVO;
import jakarta.servlet.http.HttpServletRequest;

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
    Long teamCreate(TeamCreateRequest request, User loginUser) throws Exception;

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
     * 分页查询对象构建
     *
     * @param teamQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(TeamQueryRequest teamQueryRequest);

}