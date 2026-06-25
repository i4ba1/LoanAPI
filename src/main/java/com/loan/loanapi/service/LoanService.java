package com.loan.loanapi.service;

import com.loan.loanapi.dto.request.LoanRequestDto;
import com.loan.loanapi.dto.response.LoanApprovalResponseDto;
import com.loan.loanapi.dto.response.LoanRequestResponseDto;

public interface LoanService {

    LoanRequestResponseDto requestLoan(LoanRequestDto request);

    LoanApprovalResponseDto approveLoan(String userId, String policeNumber);
}
