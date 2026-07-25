package com.musicdreamer.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("redis://${REDIS_HOST:redis}:${REDIS_PORT:6379}")
    private String address;

    @Value("${REDIS_PASSWORD:}")
    private String password;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        if (password != null && !password.isEmpty()) {
            config.useSingleServer().setAddress(address).setPassword(password);
        } else {
            config.useSingleServer().setAddress(address);
        }
        return Redisson.create(config);
    }
}
