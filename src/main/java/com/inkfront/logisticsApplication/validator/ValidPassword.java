
// ValidPassword annotation
package com.inkfront.logisticsApplication.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;
import java.util.List;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidatorClass.class)
@Documented
public @interface ValidPassword {
    String message() default "Invalid password format";
    int minLength() default 8;
    boolean requireSpecial() default true;
    boolean requireDigit() default true;
    boolean requireUpperCase() default true;
    boolean requireLowerCase() default true;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
