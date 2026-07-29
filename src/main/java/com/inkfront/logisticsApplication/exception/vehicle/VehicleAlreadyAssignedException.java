package com.inkfront.logisticsApplication.exception.vehicle;

public class VehicleAlreadyAssignedException extends RuntimeException {
    public VehicleAlreadyAssignedException(String message) {
        super(message);
    }
}