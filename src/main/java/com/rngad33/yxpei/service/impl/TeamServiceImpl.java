package com.rngad33.yxpei.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rngad33.yxpei.constant.ErrorConstant;
import com.rngad33.yxpei.exception.MyException;
import com.rngad33.yxpei.mapper.TeamMapper;
import com.rngad33.yxpei.model.dto.team.TeamCreateRequest;
import com.rngad33.yxpei.model.dto.team.TeamEditRequest;
import com.rngad33.yxpei.model.entity.Team;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.service.TeamService;
import com.rngad33.yxpei.utils.AESUtils;
import com.rngad33.yxpei.utils.LockUtils;
import com.rngad33.yxpei.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 队伍服务实现类
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Resource
    private TeamMapper teamMapper;

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
        String teamName = request.getTeamName();
        String description = request.getDescription();
        Integer maxNum = request.getMaxNum();
        Integer needApproval = request.getNeedApproval();
        Integer status = request.getStatus();
        String teamPassword = request.getTeamPassword();
        ThrowUtils.throwIf(ObjectUtil.isNull(request),
                ErrorCodeEnum.PARAMS_ERROR, "无效的请求！");
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
        ThrowUtils.throwIf(status != 0 && status != 1,
                ErrorCodeEnum.PARAMS_ERROR, "参数无效！");
        ThrowUtils.throwIf(StrUtil.isBlank(teamPassword) || teamPassword.length() < 8,
                ErrorCodeEnum.PARAMS_ERROR, "密码无效！");

        // 加锁，操作数据库
        synchronized (LockUtils.getKeyLock(teamName)) {
            // - 名称查重
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("team_name", teamName);
            long count = teamMapper.selectCountByQuery(queryWrapper);
            if (count > 1) {
                throw new MyException(ErrorCodeEnum.PARAMS_ERROR, "队伍名称已存在！");
            }
            // - 密码加密
            String encryptedPassword = AESUtils.doEncrypt(teamPassword);
            // - 写入队伍信息
            Team team = new Team();
            team.setTeamName(teamName);
            team.setDescription(description);
            team.setMaxNum(maxNum);
            team.setLeaderId(loginUser.getId());
            team.setNeedApproval(needApproval);
            team.setStatus(status);
            team.setTeamPassword(encryptedPassword);
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
     * @param request
     * @param loginUser
     * @return
     */
    public Integer teamEdit(TeamEditRequest request, User loginUser) {

        return 0;
    }


}