package com.loan.loanapi.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class LoanRequestValidator implements ConstraintValidator<ValidLoanRequest, LoanRequestDto> {

    @Override
    public boolean isValid(LoanRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getDp() == null || dto.getMrp() == null || dto.getVehicleYear() == null) {
            // @NotNull on the individual fields already reports this
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (dto.getDp().compareTo(dto.getMrp()) >= 0) {
            context.buildConstraintViolationWithTemplate("dp must be less than mrp")
                    .addPropertyNode("dp")
                    .addConstraintViolation();
            valid = false;
        }

        int currentYear = Year.now().getValue();
        if (dto.getVehicleYear() > currentYear) {
            context.buildConstraintViolationWithTemplate("vehicle_year cannot be in the future")
                    .addPropertyNode("vehicleYear")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
