
// ValidPhone annotation
package com.inkfront.logisticsApplication.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidatorClass.class)
@Documented
public @interface ValidPhone {
    String message() default "Invalid phone number format";
    boolean nigeriaOnly() default true;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
