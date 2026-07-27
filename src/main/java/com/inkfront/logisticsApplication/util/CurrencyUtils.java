package com.inkfront.logisticsApplication.util;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyUtils {

    private static final String DEFAULT_CURRENCY = "NGN";
    private static final Locale DEFAULT_LOCALE = new Locale("en", "NG");

    public static String formatCurrency(double amount) {
        return formatCurrency(amount, DEFAULT_CURRENCY);
    }

    public static String formatCurrency(double amount, String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(DEFAULT_LOCALE);
            formatter.setCurrency(currency);
            return formatter.format(amount);
        } catch (Exception e) {
            // Fallback formatting
            return currencyCode + " " + String.format("%,.2f", amount);
        }
    }

    public static String formatCurrencySymbol(double amount, String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            return currency.getSymbol(DEFAULT_LOCALE) + " " + String.format("%,.2f", amount);
        } catch (Exception e) {
            return currencyCode + " " + String.format("%,.2f", amount);
        }
    }

    public static String getCurrencySymbol(String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            return currency.getSymbol(DEFAULT_LOCALE);
        } catch (Exception e) {
            return currencyCode;
        }
    }

    public static String getCurrencyCode(String symbol) {
        // This is a simplified mapping, expand as needed
        if (symbol.equals("₦")) return "NGN";
        if (symbol.equals("$")) return "USD";
        if (symbol.equals("€")) return "EUR";
        if (symbol.equals("£")) return "GBP";
        return symbol;
    }

    public static double roundToTwoDecimalPlaces(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    public static boolean isValidCurrencyCode(String currencyCode) {
        try {
            Currency.getInstance(currencyCode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getDefaultCurrency() {
        return DEFAULT_CURRENCY;
    }

    public static String formatAmountWithCurrency(double amount, String currencyCode) {
        if (amount < 0) {
            return "-" + formatCurrency(Math.abs(amount), currencyCode);
        }
        return formatCurrency(amount, currencyCode);
    }

    public static double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        // Implement currency conversion logic using external API
        // This is a placeholder that returns the same amount
        return amount;
    }

    public static String getDisplayCurrency(double amount, String currencyCode) {
        if (amount == 0) {
            return "₦0.00";
        }
        return formatCurrency(amount, currencyCode);
    }
}