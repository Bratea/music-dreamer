package com.musicdreamer.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 分页插件配置
 * 提供 PaginationInnerInterceptor 使 lambdaQuery().page() 的分页生效，
 * 并限制单页最大条数防止恶意大分页。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor();
        pagination.setMaxLimit(500L); // 单页最多 500 条
        pagination.setOverflow(true); // 超出页码范围时回到第一页
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
