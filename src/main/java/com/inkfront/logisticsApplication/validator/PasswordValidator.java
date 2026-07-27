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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return pattern.matcher(password).matches();
    }

    public List<String> validatePasswordStrength(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty");
            return errors;
        }

        if (password.length() < 8) {
            errors.add("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*[0-9].*")) {
            errors.add("Password must contain at least one digit");
        }

        if (!password.matches(".*[@#$%^&+=].*")) {
            errors.add("Password must contain at least one special character (@#$%^&+=)");
        }

        if (password.contains(" ")) {
            errors.add("Password cannot contain spaces");
        }

        return errors;
    }

    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&+=";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }

    public boolean isPasswordExpired(String password, long daysValid) {
        // This would require storing password creation date
        return false; // Placeholder
    }

    public String getPasswordStrength(String password) {
        List<String> errors = validatePasswordStrength(password);
        if (errors.isEmpty()) {
            return "STRONG";
        } else if (errors.size() <= 2) {
            return "MEDIUM";
        } else {
            return "WEAK";
        }
    }

    public boolean isPasswordMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}
