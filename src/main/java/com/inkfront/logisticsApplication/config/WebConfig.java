package com.inkfront.logisticsApplication.config;

import com.inkfront.logisticsApplication.interceptor.LoggingInterceptor;
import com.inkfront.logisticsApplication.interceptor.RateLimitInterceptor;
import com.inkfront.logisticsApplication.interceptor.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final RequestInterceptor requestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**");

        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**");

        registry.addInterceptor(requestInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**");
    }
}