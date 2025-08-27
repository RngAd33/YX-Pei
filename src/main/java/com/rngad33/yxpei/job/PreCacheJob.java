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
    private List<Long> mainUserList = Arrays.asList(1L);

    /**
     * 预热推荐用户（每天23:59执行）
     */
    @Scheduled(cron = "0 59 23 * * *")
    public void doPreCacheRecommendUser() {
        RLock lock = redissonClient.getLock("yxpei:precachejob:docache:lock");
        try {
            if (lock.tryLock(0, 256L, TimeUnit.SECONDS)) {
                for (Long id : mainUserList) {
                    myCacheManager.writeRedissonFromSql(id);
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