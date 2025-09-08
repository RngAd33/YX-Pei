package com.rngad33.yxpei.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rngad33.yxpei.exception.MyException;
import com.rngad33.yxpei.manager.UserManager;
import com.rngad33.yxpei.mapper.TeamMapper;
import com.rngad33.yxpei.model.dto.team.TeamExitRequest;
import com.rngad33.yxpei.model.dto.team.TeamJoinRequest;
import com.rngad33.yxpei.model.dto.team.TeamQueryRequest;
import com.rngad33.yxpei.model.entity.Team;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.entity.UserTeam;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.model.enums.team.TeamStatusEnum;
import com.rngad33.yxpei.model.vo.TeamVO;
import com.rngad33.yxpei.model.vo.UserVO;
import com.rngad33.yxpei.service.TeamService;
import com.rngad33.yxpei.service.UserService;
import com.rngad33.yxpei.service.UserTeamService;
import com.rngad33.yxpei.utils.LockUtils;
import com.rngad33.yxpei.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.rngad33.yxpei.model.entity.table.TeamTableDef.TEAM;

/**
 * 队伍服务实现类
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserManager userManager;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "reg";

    /**
     * 创建队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    @Transactional
    public Long teamCreate(Team team, User loginUser) {
        // 数据准备
        ThrowUtils.throwIf(ObjectUtil.isNull(team), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        String teamName = team.getTeamName();
        String description = team.getDescription();
        Integer maxNum = team.getMaxNum();
        Date expireTime = team.getExpireTime();
        final long leaderId = team.getLeaderId();
        Integer needApproval = team.getNeedApproval();
        String teamPassword = team.getTeamPassword();
        Integer status = team.getStatus();
        // 数据校验
        ThrowUtils.throwIf(StrUtil.isBlank(teamName) || teamName.length() > 16, ErrorCodeEnum.PARAMS_ERROR, "名称不合法！");
        ThrowUtils.throwIf(StrUtil.isNotBlank(description) && description.length() > 256, ErrorCodeEnum.PARAMS_ERROR, "描述过长！");
        ThrowUtils.throwIf(maxNum <= 0 || maxNum > 30, ErrorCodeEnum.PARAMS_ERROR, "人数超出最大限制！");
        ThrowUtils.throwIf(ObjUtil.isNotNull(expireTime) && expireTime.before(new Date()), ErrorCodeEnum.PARAMS_ERROR, "时间不能早于当前时间！");
        ThrowUtils.throwIf(needApproval != 0 && needApproval != 1, ErrorCodeEnum.PARAMS_ERROR);
        ThrowUtils.throwIf(status != 0 && status != 1 && status != 2, ErrorCodeEnum.PARAMS_ERROR);
        // - 队伍开启加密且密码非空、不过长时，执行加密
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum == null) {
            teamStatusEnum = TeamStatusEnum.PUBLIC;
        }
        String encryptedPassword = null;
        if (teamStatusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StrUtil.isNotBlank(teamPassword) && teamPassword.length() <= 32) {
                encryptedPassword = DigestUtils.md5DigestAsHex((SALT + teamPassword).getBytes(StandardCharsets.UTF_8));
            } else {
                throw new MyException(ErrorCodeEnum.PARAMS_ERROR, "开启加密必须设置合理密码！");
            }
        }
        // 加锁，操作数据库
        synchronized (LockUtils.getKeyLock(loginUser.getId())) {
            // - 同一用户最多创建5个队伍
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("leader_id", loginUser.getId());
            long count = this.count(queryWrapper);
            ThrowUtils.throwIf(count >= 5, ErrorCodeEnum.PARAMS_ERROR, "同一用户最多创建5个队伍！");
            // - 名称查重
            queryWrapper.eq("team_name", teamName);
            count = this.count(queryWrapper);
            ThrowUtils.throwIf(count > 0, ErrorCodeEnum.PARAMS_ERROR, "队伍名称已存在！");
            // - 插入队伍数据到队伍表
            team.setTeamName(teamName);
            team.setDescription(description);
            team.setMaxNum(maxNum);
            team.setExpireTime(expireTime);
            team.setLeaderId(loginUser.getId());
            team.setNeedApproval(needApproval);
            team.setStatus(status);
            team.setTeamPassword(encryptedPassword);
            boolean saveResult = this.save(team);
            Long teamId = team.getId();
            ThrowUtils.throwIf(!saveResult, ErrorCodeEnum.PARAMS_ERROR, "队伍数据插入失败！");
            // - 插入映射数据到关系表
            UserTeam userTeam = new UserTeam();
            userTeam.setUserId(leaderId);
            userTeam.setTeamId(teamId);
            userTeam.setJoinTime(new Date());
            saveResult = userTeamService.save(userTeam);
            ThrowUtils.throwIf(!saveResult, ErrorCodeEnum.PARAMS_ERROR, "映射数据插入失败！");
            // - 返回新队伍id
            return teamId;
        }
    }

    /**
     * 编辑队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    public boolean teamEdit(Team team, User loginUser) {
        long teamId = team.getId();
        String teamName = team.getTeamName();
        String teamPassword = team.getTeamPassword();
        Integer status = team.getStatus();
        ThrowUtils.throwIf(ObjectUtil.isNull(team), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        ThrowUtils.throwIf(StrUtil.isBlank(teamName), ErrorCodeEnum.PARAMS_ERROR, "名称不能为空！");
        ThrowUtils.throwIf(teamId <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的id！");
        // - 队伍开启加密且密码非空、不过长时，执行加密
        String encryptedPassword;
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum == null) {
            teamStatusEnum = TeamStatusEnum.PUBLIC;
        }
        if (teamStatusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StrUtil.isNotBlank(teamPassword) && teamPassword.length() <= 32) {
                encryptedPassword = DigestUtils.md5DigestAsHex((SALT + teamPassword).getBytes(StandardCharsets.UTF_8));
                team.setTeamPassword(encryptedPassword);
            } else {
                throw new MyException(ErrorCodeEnum.PARAMS_ERROR, "开启加密必须设置合理密码！");
            }
        }
        // 加锁，操作数据库
        synchronized (LockUtils.getKeyLock(loginUser.getId())) {
            // - 同一用户最多创建5个队伍
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("leader_id", loginUser.getId());
            long count = this.count(queryWrapper);
            ThrowUtils.throwIf(count >= 5, ErrorCodeEnum.USER_LOSE_ACTION, "同一用户最多创建5个队伍！");
            // - 名称查重
            queryWrapper.eq("team_name", teamName);
            count = this.count(queryWrapper);
            ThrowUtils.throwIf(count > 1, ErrorCodeEnum.USER_LOSE_ACTION, "队伍名称已存在！");
            // - 更新数据
            return this.updateById(team);
        }
    }

    /**
     * 查询队伍列表
     *
     * @param request
     * @param isAdmin
     * @return
     */
    @Override
    public List<TeamVO> listTeams(TeamQueryRequest request, boolean isAdmin) {
        ThrowUtils.throwIf(ObjectUtil.isNull(request), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        // 多条件查询
        QueryWrapper queryWrapper = this.getQueryWrapper(request);
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        // 关联查询
        List<TeamVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long leaderId = team.getLeaderId();
            if (ObjUtil.isNull(leaderId)) {
                continue;
            }
            TeamVO teamVO = TeamVO.objToVo(team);
            ThrowUtils.throwIf(ObjUtil.isNull(teamVO), ErrorCodeEnum.PARAMS_ERROR, "数据转换失败！");
            User user = userService.getById(leaderId);
            User safeUser = userManager.getSafeUser(user);
            UserVO vo = UserVO.objToVo(safeUser);
            teamVO.setLeader(vo);
            teamUserVOList.add(teamVO);
        }
        return teamUserVOList;
    }

    /**
     * 加入队伍
     *
     * @param request
     * @param loginUser
     * @return
     */
    @Override
    public boolean teamJoin(TeamJoinRequest request, User loginUser) {
        ThrowUtils.throwIf(ObjectUtil.isNull(request), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        final long teamId = request.getTeamId();
        String teamPassword = request.getPassword();
        // 队伍信息校验
        Team team = this.getById(teamId);
        ThrowUtils.throwIf(ObjectUtil.isNull(team), ErrorCodeEnum.PARAMS_ERROR, "队伍不存在！");
        final long leaderId = team.getLeaderId();
        ThrowUtils.throwIf(ObjectUtil.isNull(teamId) || teamId <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的id！");
        // - 过期队伍不予加入
        Date expireTime = team.getExpireTime();
        ThrowUtils.throwIf(ObjectUtil.isNotNull(expireTime) && expireTime.before(new Date()),
                ErrorCodeEnum.USER_LOSE_ACTION, "队伍已过期！");
        // - 私有队伍不予加入
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        ThrowUtils.throwIf(TeamStatusEnum.PRIVATE.equals(teamStatusEnum),
                ErrorCodeEnum.USER_LOSE_ACTION, "私有队伍不可加入！");
        // - 加密队伍需要校验密码
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + teamPassword).getBytes(StandardCharsets.UTF_8));
        ThrowUtils.throwIf(StrUtil.isBlank(teamPassword) || teamPassword.equals(team.getTeamPassword()),
                ErrorCodeEnum.USER_LOSE_ACTION, "密码错误！");
        // 加分布式锁
        RLock lock = redissonClient.getLock("yxpei:team_join");
        try {
            while (true) {
                // 尝试抢锁，抢到后操作数据库
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    // - 校验已持有队伍数量
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper.eq("leader_id", leaderId);
                    long count = userTeamService.count(queryWrapper);
                    ThrowUtils.throwIf(count >= 5, ErrorCodeEnum.PARAMS_ERROR, "持有队伍数量已达上限！");
                    // - 不可重复加入已经加入的队伍
                    queryWrapper.eq("team_id", teamId);
                    count = userTeamService.count(queryWrapper);
                    ThrowUtils.throwIf(count > 0, ErrorCodeEnum.PARAMS_ERROR, "已加入该队伍！");
                    // - 是否超员
                    queryWrapper = new QueryWrapper();
                    queryWrapper.eq("team_id", teamId);
                    count = userTeamService.count(queryWrapper);
                    ThrowUtils.throwIf(count >= team.getMaxNum(), ErrorCodeEnum.PARAMS_ERROR, "队伍已满员！");
                    // - 修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(leaderId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return false;
        } finally {
            // 仅释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 退出队伍
     *
     * @param teamExitRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean teamExit(TeamExitRequest teamExitRequest, User loginUser) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamExitRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        Long teamId = teamExitRequest.getTeamId();
        Team team = this.getById(teamId);

        QueryWrapper queryWrapper = new QueryWrapper();
//        userTeamService.removeById();
        return true;
    }

    /**
     * 查询对象构建（支持分页）
     *
     * @param request
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(TeamQueryRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(request), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        Long id = request.getId();
        String teamName = request.getTeamName();
        String description = request.getDescription();
        final long leaderId = request.getLeaderId();
        Integer status = request.getStatus();
        String searchText = request.getSearchText();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", id, ObjUtil.isNotNull(id) && id > 0);
        queryWrapper.like("team_name", teamName, StrUtil.isNotBlank(teamName));
        queryWrapper.like("description", description, StrUtil.isNotBlank(description));
        queryWrapper.eq("leader_id", leaderId, ObjUtil.isNotNull(leaderId) && leaderId > 0);
        queryWrapper.eq("status", status, ObjUtil.isNotNull(status) && status > -1);
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(TEAM.TEAM_NAME.like(searchText).or(TEAM.DESCRIPTION.like(searchText)));
        }
        // 过期队伍不予展示
        queryWrapper.and(TEAM.EXPIRE_TIME.gt(new Date()).or(TEAM.EXPIRE_TIME.isNull()));
        return queryWrapper;
    }

}