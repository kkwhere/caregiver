package com.ck0731.caregiver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 定义跨域配置对象
        CorsConfiguration config = new CorsConfiguration();

        // 2. 允许所有域名访问 (开发环境用 "*"，生产环境建议指定具体域名)
        config.addAllowedOriginPattern("*");

        // 3. 允许发送 Cookie (如果需要登录态)
        config.setAllowCredentials(true);

        // 4. 允许所有请求头
        config.addAllowedHeader("*");

        // 5. 允许所有请求方法 (GET, POST, PUT, DELETE 等)
        config.addAllowedMethod("*");

        // 6. 预检请求缓存时间
        config.setMaxAge(3600L);

        // 7. 注册路径，对所有接口生效
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}