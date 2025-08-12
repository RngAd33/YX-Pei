package com.rngad33.yxpei.controller;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.mybatisflex.core.paginate.Page;
import com.rngad33.yxpei.annotation.AuthCheck;
import com.rngad33.yxpei.constant.UserConstant;
import com.rngad33.yxpei.exception.MyException;
import com.rngad33.yxpei.common.BaseResponse;
import com.rngad33.yxpei.model.dto.*;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.manager.UserManager;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.vo.UserVO;
import com.rngad33.yxpei.service.UserService;
import com.rngad33.yxpei.utils.ResultUtils;
import com.rngad33.yxpei.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserManager userManager;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册请求体
     * @return id
     * @throws Exception
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest)
            throws Exception {
        ThrowUtils.throwIf(ObjUtil.isNull(userRegisterRequest), ErrorCodeEnum.USER_LOSE_ACTION, "无效的请求！");
        String userName = userRegisterRequest.getUserName();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 校验参数（倾向于对参数本身的校验，不涉及业务逻辑）
        if (StringUtils.isAnyBlank(userName, userPassword, checkPassword)) {
            throw new MyException(ErrorCodeEnum.USER_LOSE_ACTION);
        }
        Long result = userService.userRegister(userName, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录请求体
     * @return 脱敏后的账户信息
     * @throws Exception
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                        HttpServletRequest request) throws Exception {
        ThrowUtils.throwIf(ObjUtil.isNull(userLoginRequest), ErrorCodeEnum.USER_LOSE_ACTION, "无效的请求！");
        String userName = userLoginRequest.getUserName();
        String userPassword = userLoginRequest.getUserPassword();
        // 校验参数
        if (StringUtils.isAnyBlank(userName, userPassword)) {
            throw new MyException(ErrorCodeEnum.USER_LOSE_ACTION);
        }
        User user = userService.userLogin(userName, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 获取当前用户登录态
     *
     * @param request http请求
     * @return 登录态
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(request), ErrorCodeEnum.USER_LOSE_ACTION, "HTTP请求无效！");
        User user = userService.getCurrentUser(request);
        return ResultUtils.success(user);
    }

    /**
     * 退出登录
     *
     * @param request http请求
     * @return 状态码
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        Integer result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 用户模糊查询（基于用户名）
     *
     * @param userName 用户名
     * @return 用户列表
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String userName, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(userName), ErrorCodeEnum.NO_PARAMS, "用户名不能为空！");
        List<User> users = userService.searchUsers(userName, request);
        return ResultUtils.success(users);
    }

    /**
     * 根据id查询用户（管理员）
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCodeEnum.PARAMS_ERROR, "id无效！");
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCodeEnum.NO_PARAMS);
        return ResultUtils.success(user);
    }

    /**
     * 根据id查询用户（用户）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCodeEnum.PARAMS_ERROR, "id无效！");
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 根据标签查询用户（用户）
     *
     * @param tags
     * @return
     */
    @GetMapping("/get/tags/")
    public BaseResponse<List<User>> getUserByTags(List<String> tags) {
        ThrowUtils.throwIf(CollectionUtils.isEmpty(tags), ErrorCodeEnum.NO_PARAMS, "标签列表为空！");
        return ResultUtils.success(userService.searchUsersByTags(tags));
    }

    /**
     * 分页查询用户列表
     *
     * @param userQueryRequest 用户查询请求对象
     * @return userVOPage
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUsersByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(ObjUtil.isNull(userQueryRequest), ErrorCodeEnum.USER_LOSE_ACTION, "无效的请求！");
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotalPage());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 添加用户（仅管理员）
     *
     * @param userAddRequest
     * @return
     * @throws Exception
     */
    @PostMapping("/admin/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) throws Exception {
        ThrowUtils.throwIf(ObjUtil.isNull(userAddRequest), ErrorCodeEnum.USER_LOSE_ACTION, "无效的请求！");
        return ResultUtils.success(userService.addUser(userAddRequest));
    }

    /**
     * 用户封禁 / 解封（仅管理员）
     *
     * @param userManageRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/admin/ban")
    public BaseResponse<Integer> userOrBan(@RequestBody UserManageRequest userManageRequest,
                                           HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(userManageRequest), ErrorCodeEnum.USER_LOSE_ACTION, "无效的请求！");
        Long id = userManager.getId(userManageRequest, request);
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCodeEnum.PARAMS_ERROR, "id无效！");
        Integer result = userService.userOrBan(id, request);
        return ResultUtils.success(result);
    }

    /**
     * 用户删除（仅管理员，逻辑删除）
     *
     * @param userManageRequest 用户管理请求体
     * @return 删除结果
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/admin/delete")
    public BaseResponse<Boolean> userDelete(@RequestBody UserManageRequest userManageRequest) {
        if (userManageRequest == null || userManageRequest.getId() == null) {
            throw new MyException(ErrorCodeEnum.PARAMS_ERROR);
        }
        Long id = userManageRequest.getId();
        boolean result = userService.removeById(id);   // 无需业务层
        ThrowUtils.throwIf(!result, ErrorCodeEnum.USER_LOSE_ACTION);
        return ResultUtils.success(true);
    }

    /**
     * 用户更新（仅管理员）
     *
     * @param userUpdateRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/admin/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new MyException(ErrorCodeEnum.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCodeEnum.USER_LOSE_ACTION);
        return ResultUtils.success(true);
    }

}