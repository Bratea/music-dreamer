package com.musicdreamer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.musicdreamer.mapper")
public class PlaylistServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlaylistServiceApplication.class, args);
    }
}
