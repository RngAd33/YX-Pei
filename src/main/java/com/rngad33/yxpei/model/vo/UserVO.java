package com.rngad33.yxpei.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mybatisflex.annotation.ColumnMask;
import com.mybatisflex.core.mask.Masks;
import com.rngad33.yxpei.model.entity.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 用户视图
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVO {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 性别：0-女，1-男
     */
    private Integer gender;

    /**
     * 电话
     */
    @ColumnMask(Masks.FIXED_PHONE)
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 用户状态：0-正常，1-封禁
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 封装类转对象
     *
     * @param userVO
     * @return
     */
    public static User voToObj(UserVO userVO) {
        if (userVO == null) {
            return null;
        }
        User user = new User();
        BeanUtil.copyProperties(userVO, user);
        return user;
    }

    /**
     * 对象转封装类
     * 获得脱敏后的用户信息
     *
     * @param user
     * @return
     */
    public static UserVO objToVo(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

}