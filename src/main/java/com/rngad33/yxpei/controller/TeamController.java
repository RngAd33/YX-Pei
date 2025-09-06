package com.rngad33.yxpei.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rngad33.yxpei.annotation.AuthCheck;
import com.rngad33.yxpei.common.BaseResponse;
import com.rngad33.yxpei.constant.UserConstant;
import com.rngad33.yxpei.manager.MyCacheManager;
import com.rngad33.yxpei.manager.UserManager;
import com.rngad33.yxpei.model.dto.team.*;
import com.rngad33.yxpei.model.entity.Team;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.model.vo.TeamVO;
import com.rngad33.yxpei.service.TeamService;
import com.rngad33.yxpei.service.UserService;
import com.rngad33.yxpei.utils.ResultUtils;
import com.rngad33.yxpei.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private MyCacheManager myCacheManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserManager userManager;

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    /**
     * 创建队伍
     *
     * @return
     */
    @PostMapping("/create")
    public BaseResponse<Long> teamCreate(@RequestBody TeamCreateRequest teamCreateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamCreateRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        // 必须登录才能操作
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        Long result = teamService.teamCreate(teamCreateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 编辑队伍（仅用户）
     *
     * @param teamEditRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @PostMapping("/edit")
    public BaseResponse<Integer> teamEdit(@RequestBody TeamEditRequest teamEditRequest, HttpServletRequest request)
            throws Exception {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamEditRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        boolean isAdmin = userManager.isAdmin(loginUser);
        // 必须登录才能操作
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        // 仅队长有权编辑；队长只能编辑自己创建的队伍
        ThrowUtils.throwIf(ObjectUtil.notEqual(loginUser.getId(), teamEditRequest.getLeaderId()) && !isAdmin,
                ErrorCodeEnum.USER_NOT_AUTH, "队员不可删除队伍！");
        Team team = new Team();
        BeanUtil.copyProperties(teamEditRequest, team);
        Integer result = teamService.teamEdit(team, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 查询队伍列表
     *
     * @param teamQueryRequest
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamVO>> listTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamQueryRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        String teamName = teamQueryRequest.getTeamName();
        long leaderId = teamQueryRequest.getLeaderId();
        ThrowUtils.throwIf(StrUtil.isBlank(teamName) && leaderId <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的参数！");
        User loginUser = userService.getCurrentUser(request);
        boolean isAdmin = userManager.isAdmin(loginUser);
        List<TeamVO> teamList = teamService.listTeams(teamQueryRequest, isAdmin);
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询队伍列表
     *
     * @param teamQueryRequest
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQueryRequest teamQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamQueryRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        QueryWrapper queryWrapper = new QueryWrapper();
        Page<Team> page = new Page<>(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize());
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    /**
     * 删除队伍
     *
     * @param teamManageRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> teamDelete(@RequestBody TeamManageRequest teamManageRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamManageRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        boolean isAdmin = userManager.isAdmin(loginUser);
        // 仅管理员和队长有权删除；队长只能删除自己创建的队伍，管理员可删除任何队伍
        ThrowUtils.throwIf(ObjectUtil.notEqual(loginUser.getId(), teamManageRequest.getLeaderId()) && !isAdmin,
                ErrorCodeEnum.USER_NOT_AUTH, "队员不可删除队伍！");
        Boolean result = teamService.removeById(teamManageRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新队伍（仅管理员）
     *
     * @param teamUpdateRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> teamUpdate(@RequestBody TeamUpdateRequest teamUpdateRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamUpdateRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        long leaderId = teamUpdateRequest.getLeaderId();
        String teamName = teamUpdateRequest.getTeamName();
        ThrowUtils.throwIf(StrUtil.isBlank(teamName) && leaderId <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的参数！");
        Team team = new Team();
        BeanUtil.copyProperties(teamUpdateRequest, team);
        Boolean result = teamService.updateById(team);
        return ResultUtils.success(result);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> teamJoin(TeamJoinRequest teamJoinRequest, HttpServletRequest request)
            throws Exception {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamJoinRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        Boolean result = teamService.teamJoin(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 退出队伍
     *
     * @param teamExitRequest
     * @param request
     * @return
     */
    @PostMapping("/exit")
    public BaseResponse<Boolean> teamExit(TeamExitRequest teamExitRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamExitRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        Boolean result = teamService.teamExit(teamExitRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户创建的队伍
     *
     * @param teamQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/teams/create")
    public BaseResponse<List<TeamVO>> getCurrentCreatedTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamQueryRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        teamQueryRequest.setLeaderId(loginUser.getId());
        List<TeamVO> teamList = teamService.listTeams(teamQueryRequest, true);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取当前用户加入的队伍
     *
     * @param teamQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/teams/join")
    public BaseResponse<List<TeamVO>> getCurrentJoinedTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamQueryRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);

        return ResultUtils.success(null);
    }

}