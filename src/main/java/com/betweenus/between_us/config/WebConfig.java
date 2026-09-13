package com.betweenus.between_us.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(
            InterceptorRegistry registry
    ) {

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/chat.html",
                        "/memories.html",
                        "/love-letter.html",
                        "/media.html",
                        "/api/messages/**",
                        "/api/typing/**",
                        "/api/media/**"
                )
                .excludePathPatterns(
                        "/",
                        "/index.html",
                        "/login.html",
                        "/api/auth/**",
                        "/css/**",
                        "/js/**",
                        "/uploads/**",
                        "/favicon.ico"
                );
    }

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        registry.addResourceHandler(
                "/uploads/**"
        ).addResourceLocations(
                "file:uploads/"
        );
    }
}