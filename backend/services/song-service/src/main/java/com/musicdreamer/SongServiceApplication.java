package com.musicdreamer;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {RedissonAutoConfigurationV2.class})
@EnableFeignClients
@MapperScan("com.musicdreamer.mapper")
public class SongServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SongServiceApplication.class, args);
    }
}
