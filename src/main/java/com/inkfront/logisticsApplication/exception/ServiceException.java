package com.inkfront.logisticsApplication.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public ServiceException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        this.args = null;
    }

    public ServiceException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public ServiceException(String message, ErrorCode errorCode, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        this.args = null;
    }

    public ServiceException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }
}