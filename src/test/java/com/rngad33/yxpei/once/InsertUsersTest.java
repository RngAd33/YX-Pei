package com.rngad33.yxpei.once;

import cn.hutool.core.date.StopWatch;
import com.rngad33.yxpei.mapper.UserMapper;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * 该测试类用于向数据库批量插入数据
 */
@SpringBootTest
class InsertUsersTest {

    @Resource
    private UserService userService;

    @Test
    void doInsert() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        List<User> users = new ArrayList<>();
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
            users.add(user);
        }
        userService.saveBatch(users, 100);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}