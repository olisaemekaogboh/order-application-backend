package com.inkfront.logisticsApplication.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {

    private String message;
    private String errorCode;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> validationErrors;
    private String stackTrace;

    public static ErrorResponse of(String message, String errorCode, int status) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(String message, String errorCode, int status, String path) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .status(status)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    public static ErrorResponse of(String message, String errorCode, int status,
                                   Map<String, String> validationErrors) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .status(status)
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}