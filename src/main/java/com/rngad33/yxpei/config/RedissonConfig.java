package com.rngad33.yxpei.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {

    private String host;

    private String port;

    private int database;

    /**
     * 初始化Redisson客户端
     *
     * @return
     */
    @Bean
    public RedissonClient redissonClient() {
        // 创建配置
        Config config = new Config();
        // - 构建Redis连接地址
        String redisAddress = String.format("redis://%s:%s", host, port);
        // - 配置Redisson连接（暂不使用集群）
        config.useSingleServer().setAddress(redisAddress).setDatabase(database);
        // 创建并返回Redisson实例
        return Redisson.create(config);
    }

}