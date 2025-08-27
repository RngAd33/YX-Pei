package com.rngad33.yxpei.cache;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Redisson测试类
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

//    private static final String PREFIX = "test:";

    @Test
    void redissonTest1() {
        // 设置模拟数据
        String key = "testKey";
        String value = "testValue";

        // 增
        redissonClient.getBucket(key).set(value);

        // 查
        String storedValue = (String) redissonClient.getBucket(key).get();
        assertEquals(value, storedValue, "——！存储的值与预期不一致！——");

        // 改
        String updateValue = "updatedValue";
        redissonClient.getBucket(key).set(updateValue);
        storedValue = (String) redissonClient.getBucket(key).get();
        assertEquals(updateValue, storedValue, "——！更新后的值与预期不一致！——");

        // 删
        redissonClient.getBucket(key).delete();
        storedValue = (String) redissonClient.getBucket(key).get();
        assertNull(storedValue, "——！删除失败！——");

        System.out.println("测试结束，运行正常>>>");
    }

    @Test
    void redissonTest2() {
        // list，数据存在本地 JVM 内存中
        List<String> list = new ArrayList<>();
        list.add("yupi");
        System.out.println("list:" + list.get(0));

        list.remove(0);

        // 数据存在 redis 的内存中
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("yupi");
        System.out.println("rlist:" + rList.get(0));
        rList.remove(0);
    }

    @Test
    void testWatchDog() {
        RLock lock = redissonClient.getLock("yxpei:precachejob:docache:lock");
        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                // todo 实际要执行的方法
                System.out.println("getLock: " + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}