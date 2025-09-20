package com.rngad33.yxpei.mapper;

import com.mybatisflex.core.BaseMapper;
import com.rngad33.yxpei.model.entity.UserTeam;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户队伍关系 映射层
 */
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {}