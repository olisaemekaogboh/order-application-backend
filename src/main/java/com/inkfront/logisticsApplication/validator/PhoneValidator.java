package com.inkfront.logisticsApplication.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Pattern;

@Component
public class PhoneValidator {

    private static final String NIGERIA_PHONE_PATTERN =
            "^(\\+?234|0)(70|71|72|73|74|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99)\\d{7}$";

    private static final String INTERNATIONAL_PHONE_PATTERN =
            "^\\+(?:[0-9] ?){6,14}[0-9]$";

    private static final Pattern nigeriaPattern = Pattern.compile(NIGERIA_PHONE_PATTERN);
    private static final Pattern internationalPattern = Pattern.compile(INTERNATIONAL_PHONE_PATTERN);

    public boolean isValidNigeriaPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return nigeriaPattern.matcher(phone).matches();
    }

    public boolean isValidInternationalPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return internationalPattern.matcher(phone).matches();
    }

    public boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return isValidNigeriaPhone(phone) || isValidInternationalPhone(phone);
    }

    public String formatNigeriaPhone(String phone) {
        if (phone == null || !isValidNigeriaPhone(phone)) {
            return phone;
        }
        // Remove any formatting
        phone = phone.replaceAll("[^0-9]", "");

        if (phone.startsWith("0")) {
            return phone;
        } else if (phone.startsWith("234")) {
            return "0" + phone.substring(3);
        } else if (phone.startsWith("+234")) {
            return "0" + phone.substring(4);
        }
        return phone;
    }

    public String formatInternationalPhone(String phone) {
        if (phone == null) {
            return null;
        }
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.startsWith("0")) {
            return "+234" + phone.substring(1);
        }
        return "+" + phone;
    }

    public String extractCountryCode(String phone) {
        if (phone == null) {
            return null;
        }
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.startsWith("234")) {
            return "234";
        } else if (phone.startsWith("0")) {
            return "234";
        } else if (phone.length() > 10) {
            return phone.substring(0, phone.length() - 10);
        }
        return null;
    }

    public String extractPhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.startsWith("234")) {
            return phone.substring(3);
        } else if (phone.startsWith("0")) {
            return phone;
        }
        return phone;
    }

    public boolean hasValidLength(String phone) {
        if (phone == null) {
            return false;
        }
        String cleaned = phone.replaceAll("[^0-9]", "");
        return cleaned.length() >= 10 && cleaned.length() <= 15;
    }
}
