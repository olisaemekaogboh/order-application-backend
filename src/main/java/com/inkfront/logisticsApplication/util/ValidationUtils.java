package com.inkfront.logisticsApplication.util;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");

    private static final Pattern IPV4_PATTERN =
            Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

    public static boolean isValidIPv4(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return IPV4_PATTERN.matcher(ip).matches();
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("\\d+");
    }

    public static boolean isAlphaNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("^[a-zA-Z0-9]+$");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[@#$%^&+=].*");
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("[<>]", "");
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        phone = phone.replaceAll("[^0-9]", "");
        return phone.length() >= 10 && phone.length() <= 15;
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.isEmpty()) {
            return false;
        }
        try {
            java.time.LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return false;
        }
        try {
            java.time.LocalDateTime.parse(dateTime);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}