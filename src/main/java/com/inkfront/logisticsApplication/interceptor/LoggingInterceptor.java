package com.inkfront.logisticsApplication.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate or get request ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // Set request ID in response header
        response.setHeader(REQUEST_ID_HEADER, requestId);

        // Set start time for request duration calculation
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());

        // Log request details
        log.info("Request: [{}] {} {} - Client IP: {} - Request ID: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString() != null ? "?" + request.getQueryString() : "",
                request.getRemoteAddr(),
                requestId
        );

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        // Not needed for REST APIs
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long startTime = (long) request.getAttribute(REQUEST_START_TIME);
        long duration = System.currentTimeMillis() - startTime;

        String requestId = response.getHeader(REQUEST_ID_HEADER);
        String logMessage = String.format(
                "Response: [%s] %s - Status: %d - Duration: %dms - Request ID: %s",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                requestId
        );

        if (ex != null) {
            log.error(logMessage, ex);
        } else if (response.getStatus() >= 400) {
            log.warn(logMessage);
        } else {
            log.info(logMessage);
        }
    }
}