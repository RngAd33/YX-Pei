package com.rngad33.yxpei.once;

import cn.hutool.core.date.StopWatch;
import com.rngad33.yxpei.model.entity.Team;
import com.rngad33.yxpei.service.TeamService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 该测试类用于向数据库批量插入数据
 */
@SpringBootTest
public class InsertTeamsTest {

    @Resource
    private TeamService teamService;

    // 自定义线程池
    private final ExecutorService executorService = new ThreadPoolExecutor(60, 1000, 10000,
            TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 并发式
     */
    @Test
    void doInsert2() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 20000;
        int j = 0;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // 分10个线程
        for (int i = 0; i < 10; i++) {
            List<Team> teams = new ArrayList<>();
            do {
                j++;
                Team team = new Team();
                team.setTeamName("祈-我ら神祖と共に歩む者なり");
                team.setDescription("");
                team.setMaxNum(3);
                team.setExpireTime(new Date(2099, 9, 3));
                team.setLeaderId(1L);
                team.setNeedApproval(0);
                team.setStatus(0);
                teams.add(team);
            } while (j % INSERT_NUM != 0);
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                teamService.saveBatch(teams, 100);
            }, executorService);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();   // 阻塞
        stopWatch.stop();   // 任务完成后才执行此句
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}