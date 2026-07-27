package com.inkfront.logisticsApplication.util;

import java.security.SecureRandom;
import java.util.UUID;

public class RandomUtils {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String NUMERIC = "0123456789";

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(secureRandom.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    public static String generateRandomNumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(NUMERIC.charAt(secureRandom.nextInt(NUMERIC.length())));
        }
        return sb.toString();
    }

    public static String generateOTP() {
        return generateRandomNumeric(6);
    }

    public static String generateReference() {
        return "REF-" + generateRandomString(8).toUpperCase();
    }

    public static String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + generateRandomNumeric(4);
    }

    public static String generateToken() {
        return generateUUID().replace("-", "");
    }

    public static int generateRandomInt(int min, int max) {
        return secureRandom.nextInt(max - min + 1) + min;
    }

    public static long generateRandomLong() {
        return secureRandom.nextLong();
    }

    public static boolean generateRandomBoolean() {
        return secureRandom.nextBoolean();
    }

    public static String generateSecurePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return password.toString();
    }
}