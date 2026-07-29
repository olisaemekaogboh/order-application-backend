package com.inkfront.logisticsApplication.exception.dispatch;

public class DriverUnavailableException extends RuntimeException {
    public DriverUnavailableException(String message) {
        super(message);
    }
}