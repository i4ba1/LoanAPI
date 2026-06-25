package com.loan.loanapi.repository;

import com.loan.loanapi.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    Optional<Loan> findFirstByUserIdAndPoliceNumberOrderByCreatedAtDesc(
            String userId, String policeNumber);
}
