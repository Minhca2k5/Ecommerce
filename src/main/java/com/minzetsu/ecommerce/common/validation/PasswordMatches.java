package com.minzetsu.ecommerce.common.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PasswordMatches {
    String message() default "Passwords do not match";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};
}
