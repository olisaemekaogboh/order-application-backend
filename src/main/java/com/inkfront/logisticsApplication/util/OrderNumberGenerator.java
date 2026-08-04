package com.inkfront.logisticsApplication.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class OrderNumberGenerator {

    private static final String PREFIX = "LOG";
    private static final String INVOICE_PREFIX = "INV";
    private static final String TRACKING_PREFIX = "TRK";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_FORMATTER_COMPACT =
            DateTimeFormatter.ofPattern("yyMMdd");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Generates a unique order number using database sequence
     * Format: LOG + yyyyMMdd + 4-digit sequence (reset daily)
     */
    @Transactional
    public synchronized String generateOrderNumber() {
        String today = LocalDate.now().format(DATE_FORMATTER);

        // Get next value from sequence
        Long sequence = jdbcTemplate.queryForObject(
                "SELECT nextval('order_number_seq')",
                Long.class
        );

        if (sequence == null) {
            log.error("Failed to get sequence value");
            return generateFallbackOrderNumber();
        }

        // Format: LOG202608030001
        String sequenceStr = String.format("%04d", sequence % 10000);
        String orderNumber = PREFIX + today + sequenceStr;

        log.debug("Generated order number: {} (sequence: {})", orderNumber, sequence);

        return orderNumber;
    }

    /**
     * Generates a unique invoice number
     * Format: INV + yyyyMMdd + 4-digit sequence
     */
    @Transactional
    public synchronized String generateInvoiceNumber() {
        String today = LocalDate.now().format(DATE_FORMATTER);

        Long sequence = jdbcTemplate.queryForObject(
                "SELECT nextval('invoice_number_seq')",
                Long.class
        );

        if (sequence == null) {
            log.error("Failed to get invoice sequence value");
            return generateFallbackInvoiceNumber();
        }

        String sequenceStr = String.format("%04d", sequence % 10000);
        String invoiceNumber = INVOICE_PREFIX + today + sequenceStr;

        log.debug("Generated invoice number: {} (sequence: {})", invoiceNumber, sequence);

        return invoiceNumber;
    }

    /**
     * Generates tracking number using timestamp + random
     * Format: TRK + yyMMdd + 6-digit random
     */
    public String generateTrackingNumber() {
        String today = LocalDate.now().format(DATE_FORMATTER_COMPACT);
        String random = String.format("%06d", (int)(Math.random() * 1000000));
        String trackingNumber = TRACKING_PREFIX + today + random;

        log.debug("Generated tracking number: {}", trackingNumber);
        return trackingNumber;
    }

    /**
     * Generates order number with custom prefix
     */
    @Transactional
    public synchronized String generateOrderNumberWithPrefix(String customPrefix) {
        Long sequence = jdbcTemplate.queryForObject(
                "SELECT nextval('order_number_seq')",
                Long.class
        );

        if (sequence == null) {
            return customPrefix + System.currentTimeMillis();
        }

        String today = LocalDate.now().format(DATE_FORMATTER);
        String sequenceStr = String.format("%04d", sequence % 10000);
        return customPrefix + today + sequenceStr;
    }

    /**
     * Fallback method when database is unavailable
     */
    private String generateFallbackOrderNumber() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return PREFIX + today + timestamp + random;
    }

    private String generateFallbackInvoiceNumber() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return INVOICE_PREFIX + today + timestamp;
    }

    public boolean isValidOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.length() != 14) {
            return false;
        }
        return orderNumber.startsWith(PREFIX);
    }

    public String extractDateFromOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.length() < 14) {
            return null;
        }
        return orderNumber.substring(3, 11);
    }

    public String extractSequenceFromOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.length() < 14) {
            return null;
        }
        return orderNumber.substring(11);
    }

    /**
     * Reset sequences (for testing or maintenance)
     */
    public void resetSequences() {
        jdbcTemplate.execute("ALTER SEQUENCE order_number_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE invoice_number_seq RESTART WITH 1");
        log.info("Reset order and invoice sequences");
    }
}