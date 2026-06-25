package com.loan.loanapi.service;

import com.loan.loanapi.dto.request.LoanRequestDto;
import com.loan.loanapi.dto.response.LoanApprovalResponseDto;
import com.loan.loanapi.dto.response.LoanRequestResponseDto;
import com.loan.loanapi.entity.Loan;
import com.loan.loanapi.enums.LoanStatus;
import com.loan.loanapi.exception.LoanAlreadyProcessedException;
import com.loan.loanapi.exception.LoanNotFoundException;
import com.loan.loanapi.repository.LoanRepository;
import com.loan.loanapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    private LoanRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new LoanRequestDto(
                "Bruce",
                BigDecimal.valueOf(100_000_000),
                BigDecimal.valueOf(20_000_000),
                2018,
                "B 1234 BYE",
                "SDR72V25000W201");
    }

    @Test
    void requestLoan_savesLoanWithSubmittedStatus() {
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(java.util.UUID.randomUUID());
            return loan;
        });

        LoanRequestResponseDto response = loanService.requestLoan(requestDto);

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        org.mockito.Mockito.verify(loanRepository).save(loanCaptor.capture());
        assertThat(loanCaptor.getValue().getStatus()).isEqualTo(LoanStatus.SUBMITTED);

        assertThat(response.getUserId()).isEqualTo("Bruce");
        assertThat(response.getLoans()).hasSize(1);
        assertThat(response.getLoans().get(0).getStatus()).isEqualTo(LoanStatus.SUBMITTED);
        assertThat(response.getLoans().get(0).getPoliceNumber()).isEqualTo("B 1234 BYE");
    }

    @Test
    void approveLoan_transitionsSubmittedLoanToApproved() {
        Loan existingLoan = Loan.builder()
                .id(java.util.UUID.randomUUID())
                .userId("Bruce")
                .policeNumber("B 1234 BYE")
                .status(LoanStatus.SUBMITTED)
                .build();

        when(loanRepository.findFirstByUserIdAndPoliceNumberOrderByCreatedAtDesc("Bruce", "B 1234 BYE"))
                .thenReturn(Optional.of(existingLoan));

        LoanApprovalResponseDto response = loanService.approveLoan("Bruce", "B 1234 BYE");

        assertThat(existingLoan.getStatus()).isEqualTo(LoanStatus.APPROVED);
        assertThat(response.getUserId()).isEqualTo("Bruce");
        assertThat(response.getPoliceNumber()).isEqualTo("B 1234 BYE");
        assertThat(response.getMessage()).isEqualTo("Loan updated successfully.");
    }

    @Test
    void approveLoan_throwsWhenLoanNotFound() {
        when(loanRepository.findFirstByUserIdAndPoliceNumberOrderByCreatedAtDesc("Bruce", "B 1234 BYE"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.approveLoan("Bruce", "B 1234 BYE"))
                .isInstanceOf(LoanNotFoundException.class)
                .hasMessage("Loan not Found");
    }

    @Test
    void approveLoan_throwsWhenLoanAlreadyApproved() {
        Loan alreadyApproved = Loan.builder()
                .id(java.util.UUID.randomUUID())
                .userId("Bruce")
                .policeNumber("B 1234 BYE")
                .status(LoanStatus.APPROVED)
                .build();

        when(loanRepository.findFirstByUserIdAndPoliceNumberOrderByCreatedAtDesc("Bruce", "B 1234 BYE"))
                .thenReturn(Optional.of(alreadyApproved));

        assertThatThrownBy(() -> loanService.approveLoan("Bruce", "B 1234 BYE"))
                .isInstanceOf(LoanAlreadyProcessedException.class);
    }
}
