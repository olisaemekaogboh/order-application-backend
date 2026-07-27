package com.inkfront.logisticsApplication.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final String errorCode;

    public UnauthorizedException(String message) {
        super(message);
        this.errorCode = "UNAUTHORIZED";
    }

    public UnauthorizedException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNAUTHORIZED";
    }
}