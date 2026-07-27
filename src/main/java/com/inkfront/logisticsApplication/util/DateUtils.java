package com.inkfront.logisticsApplication.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class DateUtils {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATETIME_FORMATTER);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER);
    }

    public static String formatTime(LocalTime time) {
        if (time == null) return null;
        return time.format(TIME_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null) return null;
        return LocalDateTime.parse(dateTime, DATETIME_FORMATTER);
    }

    public static LocalDate parseDate(String date) {
        if (date == null) return null;
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    public static LocalDateTime getStartOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay();
    }

    public static LocalDateTime getEndOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atTime(23, 59, 59);
    }

    public static LocalDate getToday() {
        return LocalDate.now();
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public static long getDaysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }

    public static long getHoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return java.time.temporal.ChronoUnit.HOURS.between(start, end);
    }

    public static LocalDate getFirstDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    public static LocalDate getLastDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1)
                .with(TemporalAdjusters.lastDayOfMonth());
    }

    public static LocalDate getFirstDayOfWeek() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate getLastDayOfWeek() {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public static boolean isWeekend(LocalDate date) {
        if (date == null) return false;
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public static boolean isHoliday(LocalDate date) {
        // Implement holiday check logic
        return false;
    }

    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String getFormattedElapsedTime(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "Unknown";
        long seconds = java.time.temporal.ChronoUnit.SECONDS.between(start, end);

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("m ");
        sb.append(secs).append("s");

        return sb.toString();
    }
}