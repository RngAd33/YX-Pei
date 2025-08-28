package com.rngad33.yxpei.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rngad33.yxpei.constant.ErrorConstant;
import com.rngad33.yxpei.exception.MyException;
import com.rngad33.yxpei.manager.UserManager;
import com.rngad33.yxpei.mapper.TeamMapper;
import com.rngad33.yxpei.model.dto.team.TeamCreateRequest;
import com.rngad33.yxpei.model.dto.team.TeamEditRequest;
import com.rngad33.yxpei.model.dto.team.TeamQueryRequest;
import com.rngad33.yxpei.model.entity.Team;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.model.vo.TeamVO;
import com.rngad33.yxpei.service.TeamService;
import com.rngad33.yxpei.utils.AESUtils;
import com.rngad33.yxpei.utils.LockUtils;
import com.rngad33.yxpei.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 队伍服务实现类
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserManager userManager;

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
        String teamName = request.getTeamName();
        String description = request.getDescription();
        Integer maxNum = request.getMaxNum();
        Date expireTime = request.getExpireTime();
        Integer needApproval = request.getNeedApproval();
        Integer status = request.getStatus();
        String teamPassword = request.getTeamPassword();
        this.doValidateForCreateOrEdit(loginUser, teamName, description, maxNum, needApproval, status);

        // 加锁，操作数据库
        synchronized (LockUtils.getKeyLock(teamName)) {
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
    public Integer teamEdit(Team team, User loginUser) {
        long teamId = team.getId();
        ThrowUtils.throwIf(ObjectUtil.isNull(team), ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
        ThrowUtils.throwIf(teamId <= 0, ErrorCodeEnum.PARAMS_ERROR, "无效的id！");
        Team oldTeam = teamMapper.selectOneById(teamId);
        // 管理员有权编辑所有队伍信息，普通用户只能编辑自己创建的队伍视图信息
        if (userManager.isNotAdmin(loginUser) && !Objects.equals(oldTeam.getLeaderId(), loginUser.getId())) {
            throw new MyException(ErrorCodeEnum.USER_NOT_AUTH);
        }
        ThrowUtils.throwIf(ObjectUtil.isNull(oldTeam), ErrorCodeEnum.NO_PARAMS, "队伍不存在！");
        return teamMapper.update(team);
    }

    /**
     * 获取队伍列表
     *
     * @param teamName
     * @param leaderId
     * @return
     */
    @Override
    public List<TeamVO> listTeams(String teamName, long leaderId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        // todo 组合查询条件


        // 关联查询
        List<Team> teamList = this.list(queryWrapper);
        List<TeamVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {

        }
        return teamUserVOList;
    }

    /**
     * 分页查询对象构建
     *
     * @param teamQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(TeamQueryRequest teamQueryRequest) {
        if (teamQueryRequest == null) {
            throw new MyException(ErrorCodeEnum.NO_PARAMS, "请求参数为空");
        }
        String teamName = teamQueryRequest.getTeamName();
        Long leaderId = teamQueryRequest.getLeaderId();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.like("team_name", teamName, StrUtil.isNotBlank(teamName));
        queryWrapper.eq("leader_id", leaderId, ObjUtil.isNotNull(leaderId));
        return queryWrapper;
    }

    /**
     * 创建、编辑队伍方法信息校验
     *
     * @param loginUser
     * @param teamName
     * @param description
     * @param maxNum
     * @param needApproval
     * @param status
     */
    private void doValidateForCreateOrEdit(User loginUser, String teamName, String description, Integer maxNum,
                                           Integer needApproval, Integer status) {
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser),
                ErrorCodeEnum.USER_NOT_LOGIN_MESSAGE);
        ThrowUtils.throwIf(StrUtil.isBlank(teamName) || teamName.length() > 16,
                ErrorCodeEnum.PARAMS_ERROR, "名称过长！");
        ThrowUtils.throwIf(StrUtil.isBlank(description) || description.length() > 256,
                ErrorCodeEnum.PARAMS_ERROR, "描述过长！");
        ThrowUtils.throwIf(maxNum <= 0 || maxNum > 30,
                ErrorCodeEnum.PARAMS_ERROR, "人数超出最大限制！");
        ThrowUtils.throwIf(needApproval != 0 && needApproval != 1,
                ErrorCodeEnum.PARAMS_ERROR, "参数无效！");
        ThrowUtils.throwIf(status != 0 && status != 1 && status != 2,
                ErrorCodeEnum.PARAMS_ERROR, "参数无效！");
    }

}