package com.inkfront.logisticsApplication.exception;

public class PaymentAlreadyCompletedException extends RuntimeException {
    public PaymentAlreadyCompletedException(String message) {
        super(message);
    }
}