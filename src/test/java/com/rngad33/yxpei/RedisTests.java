package com.rngad33.yxpei;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis测试类
 */
@SpringBootTest
class RedisTests {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void redisTest1() {
        // 获取redis操作对象
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();

        // 设置模拟数据
        String key = "testKey";
        String value = "testValue";

        // 新增、更新测试
        valueOps.set(key, value);   // 设置键值对
        String storedValue = (String) valueOps.get(key);
        assertEquals(value, storedValue, "——！存储的值与预期不一致！——");

        // 修改测试
        String updateValue = "updatedValue";
        valueOps.set(key, updateValue);
        storedValue = (String) valueOps.get(key);
        assertEquals(updateValue, storedValue, "——！更新后的值与预期不一致！——");

        // 查询测试
        storedValue = (String) valueOps.get(key);
        assertNotNull(storedValue, "——！查询失败！——");
        assertEquals(updateValue, storedValue, "——！查询的值与预期不一致！——");

        // 删除测试
        stringRedisTemplate.delete(key);
        storedValue = (String) valueOps.get(key);
        assertNull(storedValue, "——！删除失败！——");

        System.out.println("测试结束，运行正常>>>");
    }

    @Test
    public void redisTest2() {
        // 获取redis操作对象
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();

        // 设置模拟数据
        String key = "testKey";
        String value = "testValue";

        // 新增、更新测试
        valueOps.set(key, value);   // 设置键值对
        String storedValue = valueOps.get(key);
        assertEquals(value, storedValue, "——！存储的值与预期不一致！——");

        // 修改测试
        String updateValue = "updatedValue";
        valueOps.set(key, updateValue);
        storedValue = valueOps.get(key);
        assertEquals(updateValue, storedValue, "——！更新后的值与预期不一致！——");

        // 查询测试
        storedValue = valueOps.get(key);
        assertNotNull(storedValue, "——！查询失败！——");
        assertEquals(updateValue, storedValue, "——！查询的值与预期不一致！——");

        // 删除测试
        stringRedisTemplate.delete(key);
        storedValue = valueOps.get(key);
        assertNull(storedValue, "——！删除失败！——");

        System.out.println("测试结束，运行正常>>>");
    }

}