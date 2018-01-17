package com.py.modular.handler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * 应用程序入口
 *
 *@author PYSASUKE
 */
@SpringBootApplication
@EnableAutoConfiguration
@EnableCaching
@ComponentScan(basePackages = "com.py")
@MapperScan("com.py.modular.common.database.dao")
public class ModularHandler {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ModularHandler.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
