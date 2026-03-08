package com.ck0731.caregiver.config;

import com.ck0731.caregiver.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web配置类
 * 配置静态资源访问路径和拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().toString();
        registry.addResourceHandler("/" + uploadPath + "/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",           // 登录接口
                        "/user/phone-login",     // 手机登录接口
                        "/user/send-code",       // 发送验证码接口
                        "/caregiver/list",       // 护理员列表（公开）
                        "/caregiver/hot",        // 热门护理员（公开）
                        "/caregiver/search",     // 搜索护理员（公开）
                        "/caregiver/{id}",       // 护理员详情（公开）
                        "/review/caregiver/**",  // 评价列表（公开）
                        "/advertisement/**",     // 广告（公开）
                        "/hospital/**",          // 医院信息（公开）
                        "/upload/**",            // 上传文件访问（公开）
                        "/uploads/**",           // 静态资源访问（公开）
                        "/error"                 // 错误页面
                );
    }
}

