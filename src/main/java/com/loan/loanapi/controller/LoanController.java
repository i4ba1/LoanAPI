package com.loan.loanapi.controller;

import com.loan.loanapi.dto.request.LoanApprovalRequestDto;
import com.loan.loanapi.dto.request.LoanRequestDto;
import com.loan.loanapi.dto.response.LoanApprovalResponseDto;
import com.loan.loanapi.dto.response.LoanRequestResponseDto;
import com.loan.loanapi.service.IdempotencyService;
import com.loan.loanapi.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final IdempotencyService idempotencyService;

    @PostMapping
    public ResponseEntity<LoanRequestResponseDto> requestLoan(
            @Valid @RequestBody LoanRequestDto request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // Supplier is lazy — loanService.requestLoan() is only invoked inside
        // idempotencyService.execute() AFTER the key uniqueness check passes.
        Supplier<ResponseEntity<LoanRequestResponseDto>> action = () ->
                ResponseEntity.status(HttpStatus.CREATED).body(loanService.requestLoan(request));
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return action.get();
        }
        return idempotencyService.execute(idempotencyKey, LoanRequestResponseDto.class, action);
    }

    @PostMapping("/approval")
    public ResponseEntity<LoanApprovalResponseDto> approveLoan(@Valid @RequestBody LoanApprovalRequestDto request) {
        return ResponseEntity.ok(loanService.approveLoan(request.getUserId(), request.getPoliceNumber()));
    }
}
