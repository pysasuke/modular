package com.py.modular.third;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * 应用程序入口
 *
 * @author PYSASUKE
 */
@SpringBootApplication
@EnableAutoConfiguration
@EnableCaching
@ComponentScan(basePackages = "com.py")
@MapperScan("com.py.modular.common.database.dao")
@EnableMongoRepositories("com.py.modular.common.mongo.dao")
public class ModularServiceThird {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ModularServiceThird.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
