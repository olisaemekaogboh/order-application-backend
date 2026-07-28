package com.inkfront.logisticsApplication.exception.tracking;

public class InvalidTrackingStateException extends RuntimeException {
    public InvalidTrackingStateException(String message) {
        super(message);
    }
}