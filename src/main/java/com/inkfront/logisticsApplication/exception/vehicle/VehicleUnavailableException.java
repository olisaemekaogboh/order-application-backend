package com.inkfront.logisticsApplication.exception.vehicle;

public class VehicleUnavailableException extends RuntimeException {
    public VehicleUnavailableException(String message) {
        super(message);
    }
}