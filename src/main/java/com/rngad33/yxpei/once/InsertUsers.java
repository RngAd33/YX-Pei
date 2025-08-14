package com.rngad33.yxpei.once;

import cn.hutool.core.date.StopWatch;
import com.rngad33.yxpei.mapper.UserMapper;
import com.rngad33.yxpei.model.entity.User;
import jakarta.annotation.Resource;

/**
 * 批量插入用户数据
 */
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

    public void insertUsers() {
        StopWatch stopWatch = new StopWatch();
        System.out.println("goodgoodgood");
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("祈-我ら神祖と共に歩む者なり");
            user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("4444");
            user.setEmail("4444@qq.com");
            user.setTags("[]");
            user.setUserStatus(0);
            user.setRole(0);
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}