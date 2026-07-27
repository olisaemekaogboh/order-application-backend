package com.inkfront.logisticsApplication.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OrderNumberGenerator {

    private static final String PREFIX = "LOG";
    private static final AtomicLong counter = new AtomicLong(1);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generateOrderNumber() {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String sequence = String.format("%06d", counter.getAndIncrement());
        return PREFIX + date + sequence;
    }

    public String generateOrderNumberWithPrefix(String customPrefix) {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String sequence = String.format("%06d", counter.getAndIncrement());
        return customPrefix + date + sequence;
    }

    public String generateTrackingNumber() {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "TRK" + date + random;
    }

    public String generateInvoiceNumber() {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String sequence = String.format("%06d", counter.getAndIncrement());
        return "INV" + date + sequence;
    }

    public boolean isValidOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.length() < 15) {
            return false;
        }
        return orderNumber.startsWith(PREFIX);
    }

    public String extractDateFromOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.length() < 15) {
            return null;
        }
        return orderNumber.substring(3, 11);
    }

    public String extractSequenceFromOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.length() < 15) {
            return null;
        }
        return orderNumber.substring(11);
    }

    public void resetCounter() {
        counter.set(1);
    }
}