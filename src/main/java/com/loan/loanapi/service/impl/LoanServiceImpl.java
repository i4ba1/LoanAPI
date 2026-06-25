package com.loan.loanapi.service.impl;

import com.loan.loanapi.dto.request.LoanRequestDto;
import com.loan.loanapi.dto.response.LoanApprovalResponseDto;
import com.loan.loanapi.dto.response.LoanRequestResponseDto;
import com.loan.loanapi.dto.response.LoanResponseDto;
import com.loan.loanapi.entity.Loan;
import com.loan.loanapi.enums.LoanStatus;
import com.loan.loanapi.exception.LoanAlreadyProcessedException;
import com.loan.loanapi.exception.LoanNotFoundException;
import com.loan.loanapi.repository.LoanRepository;
import com.loan.loanapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    @Override
    @Transactional
    public LoanRequestResponseDto requestLoan(LoanRequestDto request) {
        Loan loan = Loan.builder()
                .userId(request.getUserId())
                .mrp(request.getMrp())
                .dp(request.getDp())
                .vehicleYear(request.getVehicleYear())
                .policeNumber(request.getPoliceNumber())
                .machineNumber(request.getMachineNumber())
                .status(LoanStatus.SUBMITTED)
                .build();

        Loan saved = loanRepository.save(loan);
        log.info("Loan {} submitted for user={} policeNumber={}", saved.getId(), saved.getUserId(),
                saved.getPoliceNumber());

        return LoanRequestResponseDto.builder()
                .userId(saved.getUserId())
                .loans(List.of(toResponseDto(saved)))
                .build();
    }

    @Override
    @Transactional
    public LoanApprovalResponseDto approveLoan(String userId, String policeNumber) {
        Loan loan = loanRepository
                .findFirstByUserIdAndPoliceNumberOrderByCreatedAtDesc(userId, policeNumber)
                .orElseThrow(() -> {
                    log.warn("Approve failed: no loan found for user={} policeNumber={}", userId, policeNumber);
                    return new LoanNotFoundException("Loan not Found");
                });

        if (loan.getStatus() != LoanStatus.SUBMITTED) {
            log.warn("Approve rejected: loan {} for user={} policeNumber={} already {}", loan.getId(), userId,
                    policeNumber, loan.getStatus());
            throw new LoanAlreadyProcessedException(
                    "Loan is already " + loan.getStatus().name().toLowerCase() + ", cannot be approved again.");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loanRepository.save(loan);
        log.info("Loan {} approved for user={} policeNumber={}", loan.getId(), userId, policeNumber);

        return LoanApprovalResponseDto.builder()
                .userId(userId)
                .policeNumber(policeNumber)
                .message("Loan updated successfully.")
                .build();
    }

    private LoanResponseDto toResponseDto(Loan loan) {
        return LoanResponseDto.builder()
                .mrp(loan.getMrp())
                .dp(loan.getDp())
                .vehicleYear(loan.getVehicleYear())
                .policeNumber(loan.getPoliceNumber())
                .machineNumber(loan.getMachineNumber())
                .status(loan.getStatus())
                .build();
    }
}
