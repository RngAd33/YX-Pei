package com.rngad33.yxpei.job;

import com.rngad33.yxpei.manager.MyCacheManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务：缓存预热
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private MyCacheManager myCacheManager;

    @Resource
    private RedissonClient redissonClient;

    // 定义重点用户
    private final List<Long> mainUserList = Arrays.asList(1L);

    // 定义重点队伍
    private final List<Long> mainTeamList = Arrays.asList(1L);

    /**
     * 预热推荐用户（每天23:59执行）
     */
    @Scheduled(cron = "0 59 23 * * *")
    public void doPreCacheRecommendUser() {
        RLock lock = redissonClient.getLock("yxpei:precachejob:docacheusers:lock");
        try {
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
                for (Long id : mainUserList) {
                    String redisKey = String.format("yxpei:user:recommend:%s", id);
                    myCacheManager.writeRedisFromSql(redisKey);
                }
            }
        } catch (InterruptedException e) {
            log.error("! Redis exe error: ", e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 预热推荐队伍（每天23:59执行）
     */
    @Scheduled(cron = "0 59 23 * * *")
    public void doPreCacheRecommendTeam() {
        RLock lock = redissonClient.getLock("yxpei:precachejob:docacheteams:lock");
        try {
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
                for (Long id : mainTeamList) {
                    String redisKey = String.format("yxpei:team:recommend:%s", id);
                    myCacheManager.writeRedisFromSql(redisKey);
                }
            }
        } catch (InterruptedException e) {
            log.error("! Redis exe error: ", e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}