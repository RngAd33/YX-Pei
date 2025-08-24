package com.rngad33.yxpei.job;

import com.rngad33.yxpei.manager.MyCacheManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 定时任务：缓存预热
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private MyCacheManager myCacheManager;

    // 定义重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    /**
     * 预热推荐用户（每天执行）
     */
    @Scheduled(cron = "0 59 23 * * ? *")
    public void doPreCacheRecommendUser() {
        for (Long id : mainUserList) {
            myCacheManager.writeCacheFromSql(id);
        }
    }

}