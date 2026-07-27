package com.inkfront.logisticsApplication.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final Object data;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.data = null;
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
    }

    public BusinessException(String message, String errorCode, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.data = null;
    }
}