package com.inkfront.logisticsApplication.validator.tracking;

import com.inkfront.logisticsApplication.exception.tracking.InvalidLocationException;

import org.springframework.stereotype.Component;

@Component
public class LocationValidator {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    public void validateCoordinates(double latitude, double longitude) {
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new InvalidLocationException("Latitude must be between -90 and 90");
        }
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new InvalidLocationException("Longitude must be between -180 and 180");
        }
    }

    public void validateAccuracy(Double accuracy) {
        if (accuracy != null && accuracy < 0) {
            throw new InvalidLocationException("Accuracy cannot be negative");
        }
    }

    public void validateSpeed(Double speed) {
        if (speed != null && speed < 0) {
            throw new InvalidLocationException("Speed cannot be negative");
        }
    }
}