package com.rngad33.yxpei.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.rngad33.yxpei.annotation.AuthCheck;
import com.rngad33.yxpei.common.BaseResponse;
import com.rngad33.yxpei.constant.UserConstant;
import com.rngad33.yxpei.manager.MyCacheManager;
import com.rngad33.yxpei.manager.UserManager;
import com.rngad33.yxpei.model.dto.team.TeamCreateRequest;
import com.rngad33.yxpei.model.dto.team.TeamEditRequest;
import com.rngad33.yxpei.model.dto.team.TeamQueryRequest;
import com.rngad33.yxpei.model.dto.team.TeamUpdateRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
@Slf4j
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
    public BaseResponse<Long> teamCreate(@RequestBody TeamCreateRequest teamCreateRequest, HttpServletRequest request)
            throws Exception {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamCreateRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        // 必须登录才能操作
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        Long result = teamService.teamCreate(teamCreateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 编辑队伍（用户）
     *
     * @param teamEditRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @PostMapping("/edit")
    public BaseResponse<Boolean> teamEdit(@RequestBody TeamEditRequest teamEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamEditRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        // 必须登录才能操作
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        // 仅队长有权编辑；队长只能编辑自己创建的队伍
        ThrowUtils.throwIf(ObjectUtil.equals(loginUser.getId(), teamEditRequest.getLeaderId()),
                ErrorCodeEnum.USER_NOT_AUTH, "队员不可编辑！");
        Team team = new Team();
        BeanUtil.copyProperties(teamEditRequest, team);
        Boolean result = teamService.updateById(team);
        return ResultUtils.success(result);
    }

    /**
     * 查询队伍列表
     *
     * @param teamQueryRequest
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<List<TeamVO>> listTeams(@RequestBody TeamQueryRequest teamQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamQueryRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        String teamName = teamQueryRequest.getTeamName();
        long leaderId = teamQueryRequest.getLeaderId();
        ThrowUtils.throwIf(StrUtil.isBlank(teamName) && leaderId <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的参数！");
        List<TeamVO> teamList = teamService.listTeams(teamName, leaderId);
        return ResultUtils.success(teamList);
    }

    /**
     * 更新队伍（仅管理员）
     *
     * @param teamUpdateRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> teamUpdate(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamUpdateRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        String teamName = teamUpdateRequest.getTeamName();
        long leaderId = teamUpdateRequest.getLeaderId();
        ThrowUtils.throwIf(StrUtil.isBlank(teamName) && leaderId <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的参数！");
        Team team = new Team();
        BeanUtil.copyProperties(teamUpdateRequest, team);
        Boolean result = teamService.updateById(team);
        return ResultUtils.success(result);
    }

}