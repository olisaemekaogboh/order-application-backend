package com.inkfront.logisticsApplication.exception.review;

public class InvalidRatingException extends RuntimeException {
    public InvalidRatingException(String message) {
        super(message);
    }
}