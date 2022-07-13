package com.example;

import cn.alphahub.dtt.plus.config.DttProperties;
import cn.alphahub.dtt.plus.enums.DbType;
import cn.alphahub.dtt.plus.util.JacksonUtil;
import cn.alphahub.dtt.plus.util.YamlToPropsUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.system.SystemUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Properties;

@SpringBootTest
class MydttPlusApplicationDemoTests {

    @Resource
    DttProperties dttProperties;

    @Test
    void contextLoads() {
    }

    @Test
    void contextLoads2() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = SystemUtil.getProps();
        Properties properties = YamlToPropsUtil.toProperties("application-db-mapper.yml");
        System.out.println(JacksonUtil.toPrettyJson(properties));
    }

    @Test
    void contextLoads3() {
        DbType dbType = DbType.getDbType(SpringUtil.getBean(DataSourceProperties.class).getUrl());
        System.out.println(dbType);
    }

    @Test
    void contextLoads4() {
        System.out.println(JacksonUtil.toPrettyJson(dttProperties));
    }

    @Test
    void contextLoads5() {
        System.out.println(dttProperties.getDataTypeMapping().getMysql().get("LocalDateTime"));
    }

}
