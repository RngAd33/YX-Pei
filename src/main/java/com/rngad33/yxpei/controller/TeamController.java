package com.rngad33.yxpei.controller;

import cn.hutool.core.util.ObjectUtil;
import com.rngad33.yxpei.common.BaseResponse;
import com.rngad33.yxpei.manager.MyCacheManager;
import com.rngad33.yxpei.model.dto.team.TeamCreateRequest;
import com.rngad33.yxpei.model.dto.team.TeamEditRequest;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.service.TeamService;
import com.rngad33.yxpei.service.UserService;
import com.rngad33.yxpei.utils.ResultUtils;
import com.rngad33.yxpei.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        ThrowUtils.throwIf(loginUser == null, ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        Long result = teamService.teamCreate(teamCreateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 编辑队伍
     *
     * @param teamEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Integer> teamEdit(@RequestBody TeamEditRequest teamEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamEditRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        User loginUser = userService.getCurrentUser(request);
        // 必须登录才能操作
        ThrowUtils.throwIf(loginUser == null, ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        // 仅队长有权编辑
        ThrowUtils.throwIf(ObjectUtil.equals(loginUser.getId(), (teamEditRequest.getLeaderId())),
                ErrorCodeEnum.USER_NOT_AUTH);
        Integer result = teamService.teamEdit(teamEditRequest, loginUser);
        return ResultUtils.success(result);
    }

}