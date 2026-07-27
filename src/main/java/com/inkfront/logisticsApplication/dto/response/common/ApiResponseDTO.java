// dto/response/common/ApiResponseDTO.java
package com.inkfront.logisticsApplication.dto.response.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ApiResponseDTO<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;
    private String path;
    private int statusCode;

    public ApiResponseDTO(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponseDTO(boolean success, String message, T data, int statusCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>(true, message, data, 200);
    }

    public static <T> ApiResponseDTO<T> success(String message) {
        return new ApiResponseDTO<>(true, message, null, 200);
    }

    public static <T> ApiResponseDTO<T> error(String message, int statusCode) {
        ApiResponseDTO<T> response = new ApiResponseDTO<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setStatusCode(statusCode);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> ApiResponseDTO<T> error(String message, String error, int statusCode) {
        ApiResponseDTO<T> response = new ApiResponseDTO<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setError(error);
        response.setStatusCode(statusCode);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}