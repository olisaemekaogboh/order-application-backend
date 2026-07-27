package com.inkfront.logisticsApplication.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;
    private final String errorCode;

    public ValidationException(String message) {
        super(message);
        this.errors = null;
        this.errorCode = "VALIDATION_ERROR";
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
        this.errorCode = "VALIDATION_ERROR";
    }

    public ValidationException(String message, String errorCode) {
        super(message);
        this.errors = null;
        this.errorCode = errorCode;
    }

    public ValidationException(String message, Map<String, String> errors, String errorCode) {
        super(message);
        this.errors = errors;
        this.errorCode = errorCode;
    }
}