package com.inkfront.logisticsApplication.interceptor;

import com.inkfront.logisticsApplication.service.interfaces.AuditService;
import com.inkfront.logisticsApplication.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Set request attributes for later use
        request.setAttribute("startTime", System.currentTimeMillis());
        request.setAttribute("clientIP", getClientIP(request));
        request.setAttribute("userAgent", request.getHeader("User-Agent"));

        // Log sensitive operations
        String method = request.getMethod();
        if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
            String userId = SecurityUtils.getCurrentUserId();
            if (userId != null) {
                auditService.logAction(
                        userId,
                        method + "_" + request.getRequestURI(),
                        "REQUEST",
                        null,
                        "Request to " + request.getRequestURI()
                );
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // Calculate response time
        long startTime = (long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        // Log slow requests
        if (duration > 5000) {
            log.warn("Slow request: {} {} took {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    duration
            );
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}