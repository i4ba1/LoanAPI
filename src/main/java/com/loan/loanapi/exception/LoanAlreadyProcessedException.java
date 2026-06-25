package com.loan.loanapi.exception;

public class LoanAlreadyProcessedException extends RuntimeException {

    public LoanAlreadyProcessedException(String message) {
        super(message);
    }
}
