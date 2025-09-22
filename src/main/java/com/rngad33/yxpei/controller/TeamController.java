package com.rngad33.yxpei.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rngad33.yxpei.annotation.AuthCheck;
import com.rngad33.yxpei.annotation.NoWriteService;
import com.rngad33.yxpei.common.BaseResponse;
import com.rngad33.yxpei.constant.UserConstant;
import com.rngad33.yxpei.manager.MyCacheManager;
import com.rngad33.yxpei.manager.UserManager;
import com.rngad33.yxpei.model.dto.team.*;
import com.rngad33.yxpei.model.entity.Team;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.entity.UserTeam;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.model.vo.TeamVO;
import com.rngad33.yxpei.service.TeamService;
import com.rngad33.yxpei.service.UserService;
import com.rngad33.yxpei.service.UserTeamService;
import com.rngad33.yxpei.utils.ResultUtils;
import com.rngad33.yxpei.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private MyCacheManager myCacheManager;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserManager userManager;

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    /**
     * 创建队伍
     *
     * @param teamCreateRequest
     * @param request
     * @return
     */
    @PostMapping("/create")
    public BaseResponse<Long> teamCreate(@RequestBody TeamCreateRequest teamCreateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamCreateRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        // 必须登录才能操作
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        Team team = new Team();
        BeanUtil.copyProperties(teamCreateRequest, team);
        Long result = teamService.teamCreate(team, loginUser);
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
    public BaseResponse<Boolean> teamEdit(@RequestBody TeamEditRequest teamEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(teamEditRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        // 必须登录才能操作
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        // 仅队长有权编辑，且只能编辑自己创建的队伍
        final Long leaderId = teamEditRequest.getLeaderId();
        ThrowUtils.throwIf(ObjUtil.notEqual(loginUser.getId(), leaderId), ErrorCodeEnum.USER_NOT_AUTH, "队员不可编辑队伍！");
        Team team = new Team();
        BeanUtil.copyProperties(teamEditRequest, team);
        boolean result = teamService.teamEdit(team, loginUser);
        ThrowUtils.throwIf(!result, ErrorCodeEnum.USER_LOSE_ACTION, "更新失败！");
        return ResultUtils.success(true);
    }

    /**
     * 查询队伍列表
     *
     * @param teamQueryRequest
     * @param request
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
        ThrowUtils.throwIf(CollectionUtils.isEmpty(teamList), ErrorCodeEnum.SYSTEM_ERROR, "数据转换失败！");
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询队伍列表
     *
     * @param teamQueryRequest
     * @return
     */
    @NoWriteService
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQueryRequest teamQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamQueryRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        QueryWrapper queryWrapper = new QueryWrapper();
        Page<Team> page = new Page<>(teamQueryRequest.getCurrent(), teamQueryRequest.getPageSize());
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    /**
     * 解散队伍
     *
     * @param teamManageRequest
     * @param request
     * @return
     */
    @NoWriteService
    @PostMapping("/destroy")
    public BaseResponse<Boolean> teamDestroy(@RequestBody TeamManageRequest teamManageRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamManageRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        boolean isAdmin = userManager.isAdmin(loginUser);
        // 仅管理员和队长有权删除；队长只能删除自己创建的队伍，管理员可删除任何队伍
        ThrowUtils.throwIf(ObjectUtil.notEqual(loginUser.getId(), teamManageRequest.getLeaderId()) && !isAdmin,
                ErrorCodeEnum.USER_NOT_AUTH, "队员不可删除队伍！");
        boolean result = teamService.teamDestroy(teamManageRequest.getId(), loginUser);
        ThrowUtils.throwIf(!result, ErrorCodeEnum.USER_LOSE_ACTION, "删除失败！");
        return ResultUtils.success(true);
    }

    /**
     * 更新队伍（仅管理员）
     *
     * @param teamUpdateRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @NoWriteService
    @PostMapping("/update")
    public BaseResponse<Boolean> teamUpdate(@RequestBody TeamUpdateRequest teamUpdateRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamUpdateRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        long leaderId = teamUpdateRequest.getLeaderId();
        String teamName = teamUpdateRequest.getTeamName();
        ThrowUtils.throwIf(StrUtil.isBlank(teamName) && leaderId <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的参数！");
        Team team = new Team();
        BeanUtil.copyProperties(teamUpdateRequest, team);
        boolean result = teamService.updateById(team);
        ThrowUtils.throwIf(!result, ErrorCodeEnum.USER_LOSE_ACTION, "更新失败！");
        return ResultUtils.success(true);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> teamJoin(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamJoinRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        boolean result = teamService.teamJoin(teamJoinRequest, loginUser);
        ThrowUtils.throwIf(!result, ErrorCodeEnum.USER_LOSE_ACTION, "加入失败！");
        return ResultUtils.success(true);
    }

    /**
     * 退出队伍
     *
     * @param teamExitRequest
     * @param request
     * @return
     */
    @PostMapping("/exit")
    public BaseResponse<Boolean> teamExit(@RequestBody TeamExitRequest teamExitRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamExitRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        long teamId = teamExitRequest.getTeamId();
        ThrowUtils.throwIf(teamId <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的id！");
        boolean result = teamService.teamExit(teamId, loginUser);
        ThrowUtils.throwIf(!result, ErrorCodeEnum.USER_LOSE_ACTION, "退出失败！");
        return ResultUtils.success(true);
    }

    /**
     * 推荐队伍
     *
     * @param pageNum
     * @param pageSize
     * @param request
     * @return
     */
    @NoWriteService
    @GetMapping("/recommend")
    public BaseResponse<Page<Team>> recommendTeams(long pageNum, long pageSize, HttpServletRequest request) {
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0 || ObjUtil.isNull(request),
                ErrorCodeEnum.PARAMS_ERROR, "参数错误！");
        User loginUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        String redisKey = String.format("yxpei:team:recommend:%s", loginUser.getId());
        // 使用布隆过滤器判断key是否存在
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("team_recommend_bloom_filter");
        if (!bloomFilter.contains(redisKey)) {
            // - key不存在，直接返回空页面，避免缓存穿透
            return ResultUtils.success(new Page<>(pageNum, pageSize));
        }
        RMap<String, Object> cacheMap = redissonClient.getMap(redisKey);
        Page<Team> teamPage = (Page<Team>) cacheMap.get(redisKey);
        if (ObjUtil.isNotNull(teamPage)) {
            // - 缓存命中
            return ResultUtils.success(teamPage);
        }
        myCacheManager.writeRedissonFromSql(redisKey, cacheMap);
        return ResultUtils.success(teamPage);
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
        List<TeamVO> teamList = teamService.listTeams(teamQueryRequest, userManager.isAdmin(loginUser));
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
        // 根据用户id关联查询队伍
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 队伍id去重
        Map<Long, List<UserTeam>> map = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(map.keySet());
        teamQueryRequest.setIdList(idList);
        // 查询队伍列表
        List<TeamVO> teamList = teamService.listTeams(teamQueryRequest, userManager.isAdmin(loginUser));
        return ResultUtils.success(teamList);
    }

}