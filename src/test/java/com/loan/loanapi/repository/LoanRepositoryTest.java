package com.loan.loanapi.repository;

import com.loan.loanapi.entity.Loan;
import com.loan.loanapi.enums.LoanStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class LoanRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private LoanRepository loanRepository;

    @Test
    void findFirstByUserIdAndPoliceNumber_returnsMostRecentLoan() {
        Loan rejected = loanRepository.save(Loan.builder()
                .userId("Bruce")
                .policeNumber("B 1234 BYE")
                .mrp(BigDecimal.TEN)
                .dp(BigDecimal.ONE)
                .vehicleYear(2018)
                .machineNumber("OLD")
                .status(LoanStatus.REJECTED)
                .build());

        Loan submitted = loanRepository.save(Loan.builder()
                .userId("Bruce")
                .policeNumber("B 1234 BYE")
                .mrp(BigDecimal.TEN)
                .dp(BigDecimal.ONE)
                .vehicleYear(2020)
                .machineNumber("NEW")
                .status(LoanStatus.SUBMITTED)
                .build());

        var found = loanRepository.findFirstByUserIdAndPoliceNumberOrderByCreatedAtDesc("Bruce", "B 1234 BYE");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(submitted.getId());
        assertThat(found.get().getId()).isNotEqualTo(rejected.getId());
    }

    @Test
    void findFirstByUserIdAndPoliceNumber_returnsEmptyWhenNoMatch() {
        var found = loanRepository.findFirstByUserIdAndPoliceNumberOrderByCreatedAtDesc("Unknown", "X");

        assertThat(found).isEmpty();
    }
}
