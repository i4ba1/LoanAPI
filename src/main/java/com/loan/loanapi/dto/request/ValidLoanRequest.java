package com.loan.loanapi.dto.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LoanRequestValidator.class)
public @interface ValidLoanRequest {

    String message() default "Invalid loan request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
