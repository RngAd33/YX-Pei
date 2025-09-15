package com.rngad33.yxpei.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rngad33.yxpei.constant.ErrorConstant;
import com.rngad33.yxpei.constant.UserConstant;
import com.rngad33.yxpei.exception.MyException;
import com.rngad33.yxpei.manager.UserManager;
import com.rngad33.yxpei.mapper.UserMapper;
import com.rngad33.yxpei.model.dto.user.UserAddRequest;
import com.rngad33.yxpei.model.dto.user.UserQueryRequest;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.model.enums.user.UserStatusEnum;
import com.rngad33.yxpei.model.vo.UserVO;
import com.rngad33.yxpei.service.UserService;
import com.rngad33.yxpei.utils.AESUtils;
import com.rngad33.yxpei.utils.AlgorithmUtils;
import com.rngad33.yxpei.utils.SpecialCharValidator;
import com.rngad33.yxpei.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 业务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserManager userManager;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 用户注册
     *
     * @param userName 账户
     * @param userPassword 密码
     * @param checkPassword 确认密码
     * @return 新账户id
     */
    @Override
    public Long userRegister(String userName, String userPassword, String checkPassword) throws Exception {
        // 信息校验
        log.info("正在执行信息校验……");
        // - 长度限制
        ThrowUtils.throwIf(userName.length() < 3 || userPassword.length() < 8,
                ErrorCodeEnum.PARAMS_ERROR,  "名称或密码长度过短！");
        // - 账户名称不能包含特殊字符
        ThrowUtils.throwIf(!SpecialCharValidator.doHighValidate(userName), ErrorCodeEnum.PARAMS_ERROR, "--Hacker!--");
        // - 密码和确认密码必须一致
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCodeEnum.PARAMS_ERROR, "确认密码不一致！");
        // 名称查重
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", userName);
        long count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCodeEnum.PARAMS_ERROR, "账户名称已存在！");
        // 密码加密
        String encryptedPassword = AESUtils.doEncrypt(userPassword);
        ThrowUtils.throwIf(StrUtil.isBlank(encryptedPassword), ErrorCodeEnum.PARAMS_ERROR, "加密出错！");
        // 加分布式锁
        RLock lock = redissonClient.getLock("yxpei:user_register" + userName);
        try {
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    // 向数据库插入数据
                    User user = new User();
                    user.setUserName(userName);
                    user.setUserPassword(encryptedPassword);
                    boolean saveResult = this.save(user);
                    ThrowUtils.throwIf(!saveResult, ErrorCodeEnum.USER_LOSE_ACTION);
                    // 返回新账户id
                    log.info("Correct! Successfully to register>>>");
                    return user.getId();
                }
            }
        } catch (InterruptedException e) {
            log.error(ErrorConstant.USER_LOSE_ACTION_MESSAGE);
            throw new MyException(ErrorCodeEnum.PARAMS_ERROR);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 用户登录
     *
     * @param userName 账号
     * @param userPassword 密码
     * @param request http请求
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userName, String userPassword, HttpServletRequest request) throws Exception {
        // 1. 信息校验
        ThrowUtils.throwIf(request == null, ErrorCodeEnum.PARAMS_ERROR, "HTTP请求无效！");
        // - 账户名称不能包含特殊字符
        ThrowUtils.throwIf(!SpecialCharValidator.doHighValidate(userName), ErrorCodeEnum.PARAMS_ERROR, "--Hacker!--");
        // 2. 密码加密
        String encryptedPassword = AESUtils.doEncrypt(userPassword);
        // 3. 连接数据库，核对用户信息
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", userName);
        queryWrapper.eq("user_password", encryptedPassword);
        User user = userMapper.selectOneByQuery(queryWrapper);
        // - 判断用户是否存在
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCodeEnum.USER_NOT_EXIST_OR_PASSWORD_ERROR_RETRY, "用户不存在！");
        // - 判断账户是否被封禁
        ThrowUtils.throwIf(Objects.equals(user.getUserStatus(), UserStatusEnum.BAN_STATUS.getValue()),
                ErrorCodeEnum.USER_NOT_AUTH, "该用户已被封禁！");
        // 4. 信息脱敏
        User safeUser = userManager.getSafeUser(user);
        // 5. 记录用户登录态（已脱敏）
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safeUser);
        return safeUser;
    }

    /**
     * 获取当前用户登录态
     *
     * @param request http请求
     * @return 登录态
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCodeEnum.PARAMS_ERROR, "HTTP请求无效！");
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);   // 关键语句
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new MyException(ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        }
        long id = currentUser.getId();
        if (id <= 0) {
            throw new MyException(ErrorCodeEnum.USER_LOSE_ACTION);
        }
        User user = this.getById(id);
        return userManager.getSafeUser(user);
    }

    /**
     * 退出登录
     *
     * @param request http请求
     * @return 状态码
     */
    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);   // 移除登录态
        return 0;
    }

    /**
     * 用户模糊查询（基于用户名）
     *
     * @param userName 用户名
     * @param request http请求
     * @return 用户列表
     */
    @Override
    public List<User> searchUsers(String userName, HttpServletRequest request) {
        ThrowUtils.throwIf(StrUtil.isBlank(userName), ErrorCodeEnum.USER_LOSE_ACTION, "名称无效！");
        ThrowUtils.throwIf(SpecialCharValidator.doHighValidate(userName), ErrorCodeEnum.PARAMS_ERROR, "--Hacker!--");
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.like("user_name", userName);
        List<User> userList = this.list(queryWrapper);
        return userList.stream()
                .filter(user -> !Objects.equals(user.getRole(), UserConstant.ADMIN_ROLE))   // 过滤管理员账户
                .map(userManager::getSafeUser)   // 信息脱敏
                .collect(Collectors.toList());
    }

    /**
     * 根据标签查询用户（基于内存过滤）
     *
     * @param tags 用户标签
     * @return 用户列表
     */
    @Override
    public List<User> searchUsersByTags(List<String> tags) {
        // 1. 查询所有用户
        QueryWrapper queryWrapper = new QueryWrapper();
        List<User> userList = this.list(queryWrapper);
        // 2. 在内存中筛选出带有目标标签的用户（采用语法糖写法）
        return userList.stream()
                .filter(user -> !Objects.equals(user.getRole(), UserConstant.ADMIN_ROLE))   // 过滤管理员账户
                .filter(user -> {   // 筛选目标标签用户
                    String tagsStr = user.getTags();
                    if (StrUtil.isBlank(tagsStr)) return false;
                    List<String> tempTags = JSONUtil.toBean(tagsStr, List.class);
                    for (String tag : tempTags) {
                        if (!tempTags.contains(tag)) return false;
                    }
                    return true;
                })
                .map(userManager::getSafeUser)   // 信息脱敏
                .collect(Collectors.toList());
    }

    /**
     * 分页查询对象构建
     *
     * @param userQueryRequest 用户查询请求对象
     * @return QueryWrapper 查询条件构造器
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new MyException(ErrorCodeEnum.NO_PARAMS, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        Integer role = userQueryRequest.getRole();
        String phone = userQueryRequest.getPhone();
        String email = userQueryRequest.getEmail();
        Integer userStatus = userQueryRequest.getUserStatus();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", id, ObjUtil.isNotNull(id));
        queryWrapper.like("user_name", userName, StrUtil.isNotBlank(userName));
        queryWrapper.eq("role", role, ObjUtil.isNotNull(role));
        queryWrapper.eq("phone", phone, StrUtil.isNotBlank(phone));
        queryWrapper.eq("email", email, StrUtil.isNotBlank(email));
        queryWrapper.eq("user_status", userStatus, ObjUtil.isNotNull(userStatus));
        return queryWrapper;
    }

    /**
     * 添加用户（仅管理员）
     *
     * @param userAddRequest
     * @return
     * @throws Exception
     */
    @Override
    public Long addUser(UserAddRequest userAddRequest) throws Exception {
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = AESUtils.doEncrypt(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCodeEnum.USER_LOSE_ACTION);
        return user.getId();
    }

    /**
     * 用户封禁 / 解封（仅管理员）
     *
     * @param id 待操作用户id
     * @return 状态码
     */
    @Override
    public Integer userOrBan(Long id, HttpServletRequest request) {
        // 1. 查询用户是否存在
        User user = userMapper.selectOneById(id);
        ThrowUtils.throwIf(user == null, ErrorCodeEnum.PARAMS_ERROR, "用户不存在！");
        // 2. 切换用户状态
        int currentStatus = user.getUserStatus();
        int newStatus = (currentStatus == 0) ? 1 : 0;
        user.setUserStatus(newStatus);
        // 3. 更新数据库
        boolean updateResult = this.updateById(user);
        ThrowUtils.throwIf(!updateResult, ErrorCodeEnum.USER_LOSE_ACTION, "封禁操作失败！");
        // 4. 返回操作结果
        if (newStatus != 0) {
            log.info("用户已封禁>>>");
        } else {
            log.info("用户已解封>>>");
        }
        return 0;
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public Integer updateUser(User user, User loginUser) {
        long id = user.getId();
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        ThrowUtils.throwIf(id <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的id！");
        // 管理员有权更新所有用户，普通用户只能更新自己的视图信息
        if (userManager.isNotAdmin(loginUser) && id != loginUser.getId()) {
            throw new MyException(ErrorCodeEnum.USER_NOT_AUTH);
        }
        User oldUser = userMapper.selectOneById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCodeEnum.NO_PARAMS, "用户不存在！");
        return userMapper.update(user);
    }

    /**
     * 用户推荐
     *
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<UserVO> recommendUsers(long num, User loginUser) {
        // 构造查询条件：筛选有标签的用户，仅查询 id 和 tags 字段
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        // 获取当前用户的标签列表
        String tags = loginUser.getTags();
        List<String> tagList = JSONUtil.toBean(tags, List.class, true);
        // 用户列表下标 -> 相似度
        Map<Double, User> distanceUserMap = new HashMap<>();
        // 依次计算所有用户和当前用户的相似度
        for (User user : userList) {
            String userTags = user.getTags();
            List<String> userTagList = JSONUtil.toBean(userTags, List.class, true);
            // - 忽略当前用户和无标签者
            if (CollectionUtil.isEmpty(userTagList) || ObjUtil.equals(user.getId(), loginUser.getId())) continue;
            // - 计算标签之间的最小编辑距离作为相似度
            double distance = AlgorithmUtils.comprehensiveSimilarity(tagList, userTagList);
            distanceUserMap.put(distance, user);
            System.out.println(user.getId() + ": " + distance);
        }
        // 取前num个用户进行相似度排序（按编辑距离）
        List<User> topUserList = distanceUserMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(num)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> idList = topUserList.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        // 查询完整用户信息并过滤掉当前用户
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        queryWrapper.clear();
        queryWrapper.in("id", idList);
        Map<Long, List<User>> map = this.list(queryWrapper)
                .stream()
                .filter(user -> !user.getId().equals(loginUser.getId()))
                .map(userManager::getSafeUser)
                .collect(Collectors.groupingBy(User::getId));
        // 按照原始顺序构建最终的用户视图对象列表
        List<UserVO> finalUserList = new ArrayList<>();
        for (Long id : idList) {
            List<User> users = map.get(id);
            if (users != null && !users.isEmpty()) {
                finalUserList.add(UserVO.objToVo(users.get(0)));
            }
        }
        return finalUserList;
    }

    /**
     * 获取单个用户信息
     *
     * @param user
     * @return 用户视图
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取用户列表
     *
     * @param userList
     * @return 用户视图列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

}