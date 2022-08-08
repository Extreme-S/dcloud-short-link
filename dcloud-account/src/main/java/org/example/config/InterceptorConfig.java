package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginInterceptor())
                //添加拦截的路径
                .addPathPatterns(
                        "/api/account/*/**", "/api/traffic/*/**")

                //排除不拦截
                .excludePathPatterns(
                        "/api/account/*/register", "/api/account/*/upload", "/api/account/*/login",
                        "/api/notify/v1/captcha", "/api/notify/*/send_code", "/api/traffic/*/reduce");
    }
}
