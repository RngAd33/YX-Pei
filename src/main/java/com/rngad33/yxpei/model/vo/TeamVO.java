package com.rngad33.yxpei.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rngad33.yxpei.model.entity.Team;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 队伍视图
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamVO {

    /**
     * 队伍id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 人数上限
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人id（队长）
     */
    private Long leaderId;

    /**
     * 创建人信息
     */
    private UserVO leader;

    /**
     * 成员列表
     */
    private List<UserVO> memberList;

    /**
     * 是否需要队长审批？0-不需要，1-需要
     */
    private Integer needApproval;

    /**
     * 开放状态？0-公开，1-私有，2-加密
     */
    private Integer status;

    /**
     * 加入密码
     */
    private String teamPassword;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 对象转封装类
     *
     * @param team
     * @return
     */
    public static TeamVO objToVo(Team team) {
        if (ObjUtil.isNull(team)) {
            return null;
        }
        TeamVO teamVO = new TeamVO();
        BeanUtil.copyProperties(team, teamVO);
        return teamVO;
    }

    /**
     * 封装类转对象
     *
     * @param teamVO
     * @return
     */
    public static Team voToObj(TeamVO teamVO) {
        if (ObjUtil.isNull(teamVO)) {
            return null;
        }
        Team team = new Team();
        BeanUtil.copyProperties(teamVO, team);
        return team;
    }

}