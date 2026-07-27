package com.inkfront.logisticsApplication.exception;

import lombok.Getter;

@Getter
public class DataAccessException extends RuntimeException {

    private final String errorCode;
    private final String operation;
    private final String entity;

    public DataAccessException(String message) {
        super(message);
        this.errorCode = "DATA_ACCESS_ERROR";
        this.operation = null;
        this.entity = null;
    }

    public DataAccessException(String message, String operation, String entity) {
        super(message);
        this.errorCode = "DATA_ACCESS_ERROR";
        this.operation = operation;
        this.entity = entity;
    }

    public DataAccessException(String message, String operation, String entity, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
        this.entity = entity;
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DATA_ACCESS_ERROR";
        this.operation = null;
        this.entity = null;
    }
}