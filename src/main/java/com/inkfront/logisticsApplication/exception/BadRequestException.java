package com.inkfront.logisticsApplication.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {

    private final String errorCode;

    public BadRequestException(String message) {
        super(message);
        this.errorCode = "BAD_REQUEST";
    }

    public BadRequestException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BAD_REQUEST";
    }
}