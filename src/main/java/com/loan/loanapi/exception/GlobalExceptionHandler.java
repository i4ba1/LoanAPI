package com.loan.loanapi.exception;

import com.loan.loanapi.dto.response.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
//import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoanNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleLoanNotFound(LoanNotFoundException ex) {
        ErrorResponseDto body = ErrorResponseDto.builder()
                .error("loan_not_found")
                .errorDescription(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(LoanAlreadyProcessedException.class)
    public ResponseEntity<ErrorResponseDto> handleLoanAlreadyProcessed(LoanAlreadyProcessedException ex) {
        ErrorResponseDto body = ErrorResponseDto.builder()
                .error("loan_already_processed")
                .errorDescription(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /*@ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponseDto body = ErrorResponseDto.builder()
                .error("invalid_credentials")
                .errorDescription("Username or password is incorrect.")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }*/

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDto> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        ErrorResponseDto body = ErrorResponseDto.builder()
                .error("concurrent_update_conflict")
                .errorDescription("Loan was updated by another request, please retry.")
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
        String description = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Invalid request");
        ErrorResponseDto body = ErrorResponseDto.builder()
                .error("invalid_request")
                .errorDescription(description)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
