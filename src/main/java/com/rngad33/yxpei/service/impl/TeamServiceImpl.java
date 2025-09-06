package com.rngad33.yxpei.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rngad33.yxpei.constant.ErrorConstant;
import com.rngad33.yxpei.exception.MyException;
import com.rngad33.yxpei.manager.UserManager;
import com.rngad33.yxpei.mapper.TeamMapper;
import com.rngad33.yxpei.model.dto.team.*;
import com.rngad33.yxpei.model.entity.Team;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.model.enums.team.TeamStatusEnum;
import com.rngad33.yxpei.model.vo.TeamVO;
import com.rngad33.yxpei.model.vo.UserVO;
import com.rngad33.yxpei.service.TeamService;
import com.rngad33.yxpei.service.UserService;
import com.rngad33.yxpei.utils.AESUtils;
import com.rngad33.yxpei.utils.LockUtils;
import com.rngad33.yxpei.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    private TeamMapper teamMapper;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 创建队伍
     *
     * @param request
     * @param loginUser
     * @return
     */
    @Override
    public Long teamCreate(TeamCreateRequest request, User loginUser) throws Exception {
        // 数据校验
        ThrowUtils.throwIf(ObjectUtil.isNull(request), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        String teamName = request.getTeamName();
        String description = request.getDescription();
        Integer maxNum = request.getMaxNum();
        Date expireTime = request.getExpireTime();
        Integer needApproval = request.getNeedApproval();
        Integer status = request.getStatus();
        String teamPassword = request.getTeamPassword();
        ThrowUtils.throwIf(StrUtil.isBlank(teamName) || teamName.length() > 16, ErrorCodeEnum.PARAMS_ERROR, "名称不合法！");
        ThrowUtils.throwIf(description.length() > 256, ErrorCodeEnum.PARAMS_ERROR, "描述过长！");
        ThrowUtils.throwIf(maxNum <= 0 || maxNum > 30, ErrorCodeEnum.PARAMS_ERROR, "人数超出最大限制！");
        ThrowUtils.throwIf(needApproval != 0 && needApproval != 1, ErrorCodeEnum.PARAMS_ERROR);
        ThrowUtils.throwIf(status != 0 && status != 1 && status != 2, ErrorCodeEnum.PARAMS_ERROR);

        // 加锁，操作数据库
        synchronized (LockUtils.getKeyLock(loginUser.getUserName())) {
            // - 名称查重
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("team_name", teamName);
            long count = teamMapper.selectCountByQuery(queryWrapper);
            if (count > 1) {
                throw new MyException(ErrorCodeEnum.PARAMS_ERROR, "队伍名称已存在！");
            }
            // - 写入队伍信息
            Team team = new Team();
            team.setTeamName(teamName);
            team.setDescription(description);
            team.setMaxNum(maxNum);
            team.setExpireTime(expireTime);
            team.setLeaderId(loginUser.getId());
            team.setNeedApproval(needApproval);
            team.setStatus(status);
            // - 队伍开启加密且密码不为空时，执行加密并写入数据库
            if (status == 2 && StrUtil.isNotBlank(teamPassword)) {
                String encryptedPassword = AESUtils.doEncrypt(teamPassword);
                team.setTeamPassword(encryptedPassword);
            }
            boolean saveResult = this.save(team);
            if (!saveResult) {
                log.error(ErrorConstant.USER_LOSE_ACTION_MESSAGE);
                throw new MyException(ErrorCodeEnum.PARAMS_ERROR);
            }
            // - 返回新队伍id
            return team.getId();
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
    public Integer teamEdit(Team team, User loginUser) throws Exception {
        long teamId = team.getId();
        String teamName = team.getTeamName();
        ThrowUtils.throwIf(ObjectUtil.isNull(team), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        ThrowUtils.throwIf(StrUtil.isBlank(teamName), ErrorCodeEnum.PARAMS_ERROR, "名称不能为空！");
        ThrowUtils.throwIf(teamId <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的id！");
        // 加锁，操作数据库
        synchronized (LockUtils.getKeyLock(loginUser.getUserName())) {
            // - 名称查重
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("team_name", teamName);
            long count = teamMapper.selectCountByQuery(queryWrapper);
            if (count > 1) {
                throw new MyException(ErrorCodeEnum.PARAMS_ERROR, "队伍名称已存在！");
            }
            // - 队伍开启加密且密码不为空时，执行加密并写入数据库
            if (team.getStatus() == 2 && StrUtil.isNotBlank(team.getTeamPassword())) {
                String encryptedPassword = AESUtils.doEncrypt(team.getTeamPassword());
                team.setTeamPassword(encryptedPassword);
            }
            return teamMapper.update(team);
        }
    }

    /**
     * 查询队伍列表
     *
     * @param teamQueryRequest
     * @return
     */
    @Override
    public List<TeamVO> listTeams(TeamQueryRequest teamQueryRequest, boolean isAdmin) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamQueryRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        // 组合多条件查询语句
        QueryWrapper queryWrapper = this.getQueryWrapper(teamQueryRequest);

        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        // 关联查询
        List<TeamVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {

            User user = userService.getById(teamQueryRequest.getLeaderId());
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);
            // 视图脱敏
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamVO.setLeader(userVO);
            }
            teamUserVOList.add(teamVO);
        }
        return teamUserVOList;
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean teamJoin(TeamJoinRequest teamJoinRequest, User loginUser) throws Exception {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamJoinRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        Long teamId = teamJoinRequest.getTeamId();
        String teamPassword = teamJoinRequest.getPassword();
        Team team = this.getById(teamId);
        Date expireTime = team.getExpireTime();
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        // 过期队伍不予加入
        ThrowUtils.throwIf(ObjectUtil.isNotNull(expireTime) && expireTime.before(new Date()),
                ErrorCodeEnum.USER_LOSE_ACTION, "队伍已过期！");
        // 私有队伍不予加入
        ThrowUtils.throwIf(TeamStatusEnum.PRIVATE.equals(teamStatusEnum),
                ErrorCodeEnum.USER_LOSE_ACTION, "私有队伍不可加入！");
        // 加密队伍需要校验密码
        String encryptedPassword = AESUtils.doEncrypt(teamPassword);
        ThrowUtils.throwIf(StrUtil.isBlank(teamPassword) || teamPassword.equals(team.getTeamPassword()),
                ErrorCodeEnum.USER_LOSE_ACTION, "密码错误！");

        // 加分布式锁，操作数据库
        RLock lock = redissonClient.getLock("yxpei:join_team");

        return null;
    }

    /**
     * 退出队伍
     *
     * @param teamExitRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean teamExit(TeamExitRequest teamExitRequest, User loginUser) {
        ThrowUtils.throwIf(ObjectUtil.isNull(teamExitRequest), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        Long teamId = teamExitRequest.getTeamId();
        Team team = this.getById(teamId);


        QueryWrapper queryWrapper = new QueryWrapper();

        return null;
    }

    /**
     * 查询对象构建（支持分页）
     *
     * @param teamQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(TeamQueryRequest teamQueryRequest) {
        if (teamQueryRequest == null) {
            throw new MyException(ErrorCodeEnum.NO_PARAMS, "无效的请求！");
        }
        Long id = teamQueryRequest.getId();
        String teamName = teamQueryRequest.getTeamName();
        String description = teamQueryRequest.getDescription();
        Long leaderId = teamQueryRequest.getLeaderId();
        Integer status = teamQueryRequest.getStatus();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", id, ObjUtil.isNotNull(id));
        queryWrapper.like("team_name", teamName, StrUtil.isNotBlank(teamName));
        queryWrapper.eq("leader_id", leaderId, ObjUtil.isNotNull(leaderId));
        queryWrapper.like("description", description, StrUtil.isNotBlank(description));
        // 过期队伍不予展示
        queryWrapper.and(TEAM.EXPIRE_TIME.gt(new Date())
                .or(TEAM.EXPIRE_TIME.isNull())
        );
        // queryWrapper.eq("status", status, ObjUtil.isNotNull(status));
        return queryWrapper;
    }

}