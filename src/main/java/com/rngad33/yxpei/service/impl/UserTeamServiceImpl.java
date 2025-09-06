package com.rngad33.yxpei.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rngad33.yxpei.mapper.UserTeamMapper;
import com.rngad33.yxpei.model.entity.UserTeam;
import com.rngad33.yxpei.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
 * 用户队伍关联服务实现类
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam> implements UserTeamService {}