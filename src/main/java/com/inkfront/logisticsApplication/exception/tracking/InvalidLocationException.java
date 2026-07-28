package com.inkfront.logisticsApplication.exception.tracking;

public class InvalidLocationException extends RuntimeException {
    public InvalidLocationException(String message) {
        super(message);
    }
}