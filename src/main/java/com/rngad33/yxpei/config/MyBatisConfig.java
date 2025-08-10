package com.rngad33.yxpei.config;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import com.mybatisflex.spring.boot.SqlSessionFactoryBeanCustomizer;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Flex 配置
 */
@Configuration
public class MyBatisConfig implements
        ConfigurationCustomizer,
        SqlSessionFactoryBeanCustomizer,
        MyBatisFlexCustomizer {

    @Override
    public void customize(FlexConfiguration flexConfiguration) {}

    @Override
    public void customize(SqlSessionFactoryBean sqlSessionFactoryBean) {}

    @Override
    public void customize(FlexGlobalConfig flexGlobalConfig) {}

}