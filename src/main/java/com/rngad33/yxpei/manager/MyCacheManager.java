package com.rngad33.yxpei.manager;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.service.UserService;
import io.netty.util.internal.ThreadLocalRandom;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 通用缓存方法
 */
@Component
@Slf4j
public class MyCacheManager {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserService userService;

    /**
     * 缓存写入（基于 Redisson）
     *
     * @param redisKey
     */
    public void writeRedissonFromSql(String redisKey) {
        // 查询数据库
        QueryWrapper queryWrapper = new QueryWrapper();
        Page<User> userPage = userService.page(new Page<>(1, 10), queryWrapper);
        // 写缓存
        try {
            RBucket<Page<User>> bucket = redissonClient.getBucket(redisKey);
            bucket.set(userPage, 60 + new Random().nextInt(50), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("! Redis set key error: ", e.getMessage());
        }
    }

    /**
     * 缓存写入（基于 Redisson）
     *
     * @param redisKey
     */
    public void writeRedissonFromSql(String redisKey, RMap<String, Object> cacheMap) {
        // 查询数据库
        QueryWrapper queryWrapper = new QueryWrapper();
        Page<User> userPage = userService.page(new Page<>(1, 10), queryWrapper);
        // 写缓存
        try {
            cacheMap.fastPut(redisKey, userPage);
            // 单独设置过期时间
            cacheMap.expire(60 + ThreadLocalRandom.current().nextInt(50), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("! Redis set key error: ", e.getMessage());
        }
    }

}