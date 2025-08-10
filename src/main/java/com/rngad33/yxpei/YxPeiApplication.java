package com.rngad33.yxpei;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类
 */
@SpringBootApplication
@MapperScan("com.rngad33.yxpei.mapper")
public class YxPeiApplication {

    public static void main(String[] args) {
        SpringApplication.run(YxPeiApplication.class, args);
        System.out.println("后端服务已启动>>>");
    }

}
