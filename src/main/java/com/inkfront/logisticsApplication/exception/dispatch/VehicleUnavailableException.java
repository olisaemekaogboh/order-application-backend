package com.inkfront.logisticsApplication.exception.dispatch;

public class VehicleUnavailableException extends RuntimeException {
    public VehicleUnavailableException(String message) {
        super(message);
    }
}