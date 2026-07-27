
// PhoneValidatorClass
package com.inkfront.logisticsApplication.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class PhoneValidatorClass implements ConstraintValidator<ValidPhone, String> {

    @Autowired
    private PhoneValidator phoneValidator;

    private boolean nigeriaOnly;

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        this.nigeriaOnly = constraintAnnotation.nigeriaOnly();
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }

        if (nigeriaOnly) {
            return phoneValidator.isValidNigeriaPhone(phone);
        }
        return phoneValidator.isValidPhone(phone);
    }
}