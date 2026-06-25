package com.loan.loanapi.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;

class LoanRequestDtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private LoanRequestDto validRequest() {
        return new LoanRequestDto(
                "Bruce",
                BigDecimal.valueOf(100_000_000),
                BigDecimal.valueOf(20_000_000),
                2018,
                "B 1234 BYE",
                "SDR72V25000W201");
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }

    @Test
    void dpGreaterThanOrEqualToMrp_isRejected() {
        LoanRequestDto request = validRequest();
        request.setDp(request.getMrp());

        var violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dp"));
    }

    @Test
    void vehicleYearInTheFuture_isRejected() {
        LoanRequestDto request = validRequest();
        request.setVehicleYear(Year.now().getValue() + 1);

        var violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("vehicleYear"));
    }
}
