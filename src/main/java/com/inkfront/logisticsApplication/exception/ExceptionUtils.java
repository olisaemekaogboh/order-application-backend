package com.inkfront.logisticsApplication.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class ExceptionUtils {

    public static String getRequestPath() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception e) {
            log.warn("Failed to get request path", e);
        }
        return null;
    }

    public static String getClientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
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
        } catch (Exception e) {
            log.warn("Failed to get client IP", e);
        }
        return null;
    }

    public static ErrorResponse buildErrorResponse(Exception ex, HttpStatus status) {
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(status.getReasonPhrase())
                .status(status.value())
                .timestamp(java.time.LocalDateTime.now())
                .path(getRequestPath())
                .build();
    }

    public static ErrorResponse buildErrorResponse(String message, String errorCode, int status) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .status(status)
                .timestamp(java.time.LocalDateTime.now())
                .path(getRequestPath())
                .build();
    }

    public static String getRootCauseMessage(Throwable ex) {
        Throwable rootCause = ex;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage();
    }

    public static boolean isClientError(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }

    public static boolean isServerError(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }

    public static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}