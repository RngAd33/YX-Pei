package com.rngad33.yxpei.once;

import cn.hutool.core.date.StopWatch;
import com.rngad33.yxpei.mapper.UserMapper;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 批量插入用户数据
 */
@Component
public class InsertUsers {

    @Resource
    private UserService userService;

    /**
     * 采用并发式插入
     */
    public void insertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        int j = 0;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // 分10个线程
        for (int i = 0; i < 10; i++) {
            List<User> users = new ArrayList<>();
            do {
                j++;
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
            } while (j % INSERT_NUM != 0);
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(users, 100);
            });
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();   // 阻塞
        stopWatch.stop();   // 任务完成后才执行
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}