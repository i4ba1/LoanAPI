package com.loan.loanapi.dto.response;

import com.loan.loanapi.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponseDto {

    private BigDecimal mrp;

    private BigDecimal dp;

    private Integer vehicleYear;

    private String policeNumber;

    private String machineNumber;

    private LoanStatus status;
}
