package com.rngad33.yxpei.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.database}")
    private int database;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${spring.data.redis.timeout}")
    private long timeout;

    /**
     * 初始化Redisson客户端
     *
     * @return
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 构建Redis连接地址
        String redisAddress = "redis://" + redisHost + ":" + redisPort;
        // 设置看门狗超时时间（默认30秒）
        config.setLockWatchdogTimeout(timeout);
        // 配置Redisson连接（暂不使用集群）
        config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(database);
        // - 如果有密码则设置密码
        if (password != null && !password.isEmpty()) {
            config.useSingleServer().setPassword(password);
        }
        // 创建并返回Redisson实例
        return Redisson.create(config);
    }

}