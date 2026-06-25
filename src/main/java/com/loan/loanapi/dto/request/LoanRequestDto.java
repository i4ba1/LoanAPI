package com.loan.loanapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidLoanRequest
public class LoanRequestDto {

    @NotBlank
    private String userId;

    @NotNull
    @Positive
    private BigDecimal mrp;

    @NotNull
    @Positive
    private BigDecimal dp;

    @NotNull
    private Integer vehicleYear;

    @NotBlank
    private String policeNumber;

    @NotBlank
    private String machineNumber;
}
