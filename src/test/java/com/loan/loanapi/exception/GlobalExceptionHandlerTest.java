package com.loan.loanapi.exception;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void loanNotFound_mapsTo404() {
        var response = handler.handleLoanNotFound(new LoanNotFoundException("Loan not Found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isEqualTo("loan_not_found");
    }

    @Test
    void loanAlreadyProcessed_mapsTo409() {
        var response = handler.handleLoanAlreadyProcessed(
                new LoanAlreadyProcessedException("Loan is already approved, cannot be approved again."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("loan_already_processed");
    }

    @Test
    void optimisticLockConflict_mapsTo409() {
        var response = handler.handleOptimisticLock(new ObjectOptimisticLockingFailureException(
                "com.loan.loanapi.entity.Loan", "1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("concurrent_update_conflict");
    }

    @Test
    @Disabled("Spring Security not on classpath; re-enable when spring-boot-starter-security is added back")
    void badCredentials_mapsTo401() {
        // handler.handleBadCredentials(new BadCredentialsException("bad creds"))
    }
}
