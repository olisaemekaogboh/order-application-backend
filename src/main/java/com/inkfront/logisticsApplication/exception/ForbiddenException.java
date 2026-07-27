package com.inkfront.logisticsApplication.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

    private final String errorCode;
    private final String resource;

    public ForbiddenException(String message) {
        super(message);
        this.errorCode = "FORBIDDEN";
        this.resource = null;
    }

    public ForbiddenException(String message, String resource) {
        super(message);
        this.errorCode = "FORBIDDEN";
        this.resource = resource;
    }

    public ForbiddenException(String message, String resource, String errorCode) {
        super(message);
        this.resource = resource;
        this.errorCode = errorCode;
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "FORBIDDEN";
        this.resource = null;
    }
}