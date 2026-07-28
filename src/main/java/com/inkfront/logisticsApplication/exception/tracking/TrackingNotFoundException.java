package com.inkfront.logisticsApplication.exception.tracking;

public class TrackingNotFoundException extends RuntimeException {
    public TrackingNotFoundException(String message) {
        super(message);
    }
}