package com.inkfront.logisticsApplication.util.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class ReportUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CURRENCY_SYMBOL = "₦";

    /**
     * Extract state from a location string (assumes format "City, State, Country").
     */
    public String extractState(String location) {
        if (location == null) return "Unknown";
        String[] parts = location.split(",");
        return parts.length >= 2 ? parts[1].trim() : "Unknown";
    }

    /**
     * Calculate revenue growth percentage between current and previous period revenue.
     */
    public double calculateRevenueGrowth(double currentRevenue, double previousRevenue) {
        if (previousRevenue == 0) return 0.0;
        return ((currentRevenue - previousRevenue) / previousRevenue) * 100;
    }

    /**
     * Format a double value as currency with the NGN symbol and two decimal places.
     */
    public String formatCurrency(Double amount) {
        if (amount == null) return CURRENCY_SYMBOL + "0.00";
        return CURRENCY_SYMBOL + String.format("%,.2f", amount);
    }

    /**
     * Safely parse a string to double, returning a default value if parsing fails.
     */
    public double safeParseDouble(String value, double defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double from '{}', using default {}", value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Round a double to two decimal places.
     */
    public double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Get the number of days between two LocalDate objects (inclusive of end date).
     */
    public long getDaysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    /**
     * Get the start and end dates for the previous period of the same length as the given range.
     */
    public LocalDate[] getPreviousPeriod(LocalDate start, LocalDate end) {
        if (start == null || end == null) return new LocalDate[]{null, null};
        long days = getDaysBetween(start, end);
        LocalDate prevStart = start.minusDays(days);
        LocalDate prevEnd = end.minusDays(days);
        return new LocalDate[]{prevStart, prevEnd};
    }

    /**
     * Format a LocalDate to a string.
     */
    public String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMAT);
    }

    /**
     * Format a LocalDateTime to a string.
     */
    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMAT);
    }

    /**
     * Generate a filename for a report.
     */
    public String generateReportFilename(String reportType, LocalDate startDate, LocalDate endDate, String extension) {
        String start = startDate != null ? startDate.format(DATE_FORMAT) : "unknown";
        String end = endDate != null ? endDate.format(DATE_FORMAT) : "unknown";
        return reportType + "_" + start + "_to_" + end + "." + extension;
    }

    /**
     * Check if a string is null or empty.
     */
    public boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}