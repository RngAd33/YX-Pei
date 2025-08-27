package com.rngad33.yxpei.model.vo;

import com.rngad33.yxpei.model.entity.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 队伍视图
 */
@Data
public class TeamVO {

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
     * 创建人（队长）
     */
    private User leader;

    /**
     * 成员表
     */
    private List<User> users;

    /**
     * 是否需要队长审批？0-不需要，1-需要
     */
    private Integer needApproval;

    /**
     * 是否公开？0-不公开，1-公开
     */
    private Integer isPublic;

    /**
     * 创建时间
     */
    private Date createTime;

}