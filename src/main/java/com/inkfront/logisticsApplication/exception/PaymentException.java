package com.inkfront.logisticsApplication.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {

    private final String transactionReference;
    private final String errorCode;

    public PaymentException(String message) {
        super(message);
        this.transactionReference = null;
        this.errorCode = "PAYMENT_ERROR";
    }

    public PaymentException(String message, String transactionReference) {
        super(message);
        this.transactionReference = transactionReference;
        this.errorCode = "PAYMENT_ERROR";
    }

    public PaymentException(String message, String transactionReference, String errorCode) {
        super(message);
        this.transactionReference = transactionReference;
        this.errorCode = errorCode;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.transactionReference = null;
        this.errorCode = "PAYMENT_ERROR";
    }
}