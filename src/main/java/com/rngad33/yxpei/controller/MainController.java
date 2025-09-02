package com.rngad33.yxpei.controller;

import com.rngad33.yxpei.annotation.AuthCheck;
import com.rngad33.yxpei.common.BaseResponse;
import com.rngad33.yxpei.constant.UserConstant;
import com.rngad33.yxpei.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全局通用接口
 */
@RestController
@RequestMapping("/")
public class MainController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 健康检查
     *
     * @return
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    /**
     * 清空缓存（仅管理员）
     *
     * @param redisKey
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/removeCache")
    public BaseResponse<Integer> removeCache(String redisKey) {
        stringRedisTemplate.delete(redisKey);
        return ResultUtils.success(null);
    }

}