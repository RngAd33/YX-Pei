package com.rngad33.yxpei.controller;

import cn.hutool.core.util.ObjUtil;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.mybatisflex.core.paginate.Page;
import com.rngad33.yxpei.annotation.AuthCheck;
import com.rngad33.yxpei.common.BaseResponse;
import com.rngad33.yxpei.constant.UserConstant;
import com.rngad33.yxpei.exception.MyException;
import com.rngad33.yxpei.manager.MyCacheManager;
import com.rngad33.yxpei.manager.UserManager;
import com.rngad33.yxpei.model.dto.user.*;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.model.vo.UserVO;
import com.rngad33.yxpei.service.UserService;
import com.rngad33.yxpei.utils.ResultUtils;
import com.rngad33.yxpei.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private MyCacheManager myCacheManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RBloomFilter<String> bloomFilter;

    @Resource
    private UserManager userManager;

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 注册请求体
     * @return id
     * @throws Exception
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) throws Exception {
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
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request)
            throws Exception {
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
        ThrowUtils.throwIf(ObjUtil.isNull(request), ErrorCodeEnum.USER_LOSE_ACTION, "HTTP请求无效！");
        Integer result = userService.userLogout(request);
        ThrowUtils.throwIf(result != 0, ErrorCodeEnum.USER_LOSE_ACTION);
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
        ThrowUtils.throwIf(ObjUtil.isNull(request), ErrorCodeEnum.USER_LOSE_ACTION, "HTTP请求无效！");
        List<User> users = userService.searchUsers(userName, request);
        return ResultUtils.success(users);
    }

    /**
     * 根据id查询用户（管理员）
     *
     * @param id
     * @return
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
        BaseResponse<User> response = this.getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 根据标签查询用户（用户）
     *
     * @param tags
     * @return
     */
    @GetMapping("/getByTags/")
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/admin/add")
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
        boolean result = userService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCodeEnum.USER_LOSE_ACTION);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户信息
     *
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new MyException(ErrorCodeEnum.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        User loginUser = userService.getCurrentUser(request);
        Integer result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 用户推荐
     *
     * @param pageNum
     * @param pageSize
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageNum, long pageSize, HttpServletRequest request) {
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0 || ObjUtil.isNull(request),
                ErrorCodeEnum.PARAMS_ERROR, "参数错误！");
        User loginUser = userService.getCurrentUser(request);
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        // 优先查询缓存
        String redisKey = String.format("yxpei:user:recommend:%s", loginUser.getId());
        // - 使用布隆过滤器判断key是否存在
        if (!bloomFilter.contains(redisKey)) {
            // - key不存在，直接返回空页面，避免缓存穿透
            return ResultUtils.success(new Page<>(pageNum, pageSize));
        }
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOps.get(redisKey);
        if (ObjUtil.isNotNull(userPage)) {
            // - 缓存命中
            return ResultUtils.success(userPage);
        }
        // - 缓存未命中，查询数据库并写入缓存
        myCacheManager.writeRedisFromSql(redisKey, valueOps);
        return ResultUtils.success(userPage);
    }

}