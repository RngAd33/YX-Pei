package com.rngad33.yxpei.manager;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rngad33.yxpei.model.entity.User;
import com.rngad33.yxpei.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    /**
     * 缓存写入
     *
     * @param id
     */
    public void writeCacheFromSql(Long id) {
        String redisKey = String.format("yxpei:user:recommend:%s", id);
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        // 查询数据库
        QueryWrapper queryWrapper = new QueryWrapper();
        Page<User> userPage = userService.page(new Page<>(1, 10), queryWrapper);
        // 写缓存
        try {
            valueOps.set(redisKey, userPage, 60 + new Random().nextInt(50), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("! Redis set key error: ", e.getMessage());
        }
    }

    /**
     * 缓存写入
     *
     * @param redisKey
     * @param valueOps
     */
    public void writeCacheFromSql(String redisKey, ValueOperations<String, Object> valueOps) {
        // 查询数据库
        QueryWrapper queryWrapper = new QueryWrapper();
        Page<User> userPage = userService.page(new Page<>(1, 10), queryWrapper);
        // 写缓存
        try {
            valueOps.set(redisKey, userPage, 60 + new Random().nextInt(50), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("! Redis set key error: ", e.getMessage());
        }
    }

}