package com.musicdreamer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Music Dreamer 悦享音乐 API")
                        .version("1.0.0")
                        .description("悦享音乐平台 RESTful API 文档，涵盖用户、歌曲、歌单、搜索等核心接口。")
                        .contact(new Contact()
                                .name("Music Dreamer Team")
                                .email("dev@musicdreamer.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Gateway 网关入口"),
                        new Server().url("http://localhost:8081").description("用户服务"),
                        new Server().url("http://localhost:8082").description("歌曲服务"),
                        new Server().url("http://localhost:8083").description("歌单服务"),
                        new Server().url("http://localhost:8084").description("搜索服务")
                ));
    }
}
