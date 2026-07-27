package com.inkfront.logisticsApplication.interceptor;

import com.inkfront.logisticsApplication.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate.limit.default.limit:100}")
    private int defaultLimit;

    @Value("${rate.limit.default.window:60}")
    private int defaultWindowSeconds;

    public RateLimitInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        if (!rateLimitEnabled) {
            return true;
        }

        String path = request.getRequestURI();

        if (path.startsWith("/actuator/health")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) {
            return true;
        }

        String clientId = getClientId(request);
        String key = "rate_limit:" + clientId + ":" + path;

        try {

            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(defaultWindowSeconds));
            }

            if (count != null && count > defaultLimit) {
                log.warn("Rate limit exceeded for client: {} on path: {}", clientId, path);
                throw new RateLimitExceededException(
                        "Rate limit exceeded. Please try again later."
                );
            }

        } catch ( DataAccessException ex) {

            log.warn("Redis unavailable. Skipping rate limiting. {}", ex.getMessage());

            // Continue processing the request instead of returning HTTP 500
            return true;
        }

        return true;
    }

    private String getClientId(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }

        Object userId = request.getAttribute("userId");

        if (userId != null) {
            return "user:" + userId;
        }

        return "ip:" + ip;
    }

    public void setLimitForPath(String path, int limit, int windowSeconds) {
        // Future implementation
    }

    public void clearRateLimit(String clientId) {
        // Future implementation
    }
}