package com.rngad33.yxpei.controller;

import com.rngad33.yxpei.annotation.AuthCheck;
import com.rngad33.yxpei.common.BaseResponse;
import com.rngad33.yxpei.constant.UserConstant;
import com.rngad33.yxpei.model.enums.misc.ErrorCodeEnum;
import com.rngad33.yxpei.utils.ResultUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全局通用接口
 */
@RestController
@RequestMapping("/")
@Slf4j
public class MainController {

    @Resource
    private RedissonClient redissonClient;

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
     * 清空Redisson缓存（仅管理员）
     *
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/removeRedisson")
    public BaseResponse<Boolean> removeRedissonCache() {
        try {
            redissonClient.getKeys().flushall();
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResultUtils.error(ErrorCodeEnum.USER_LOSE_ACTION);
        }
    }

}