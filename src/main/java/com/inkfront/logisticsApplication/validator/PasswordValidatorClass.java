
// PasswordValidatorClass
package com.inkfront.logisticsApplication.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class PasswordValidatorClass implements ConstraintValidator<ValidPassword, String> {

    @Autowired
    private PasswordValidator passwordValidator;

    private ValidPassword validPassword;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.validPassword = constraintAnnotation;
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        List<String> errors = passwordValidator.validatePasswordStrength(password);

        if (!errors.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.join(", ", errors))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}