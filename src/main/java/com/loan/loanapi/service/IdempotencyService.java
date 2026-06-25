package com.loan.loanapi.service;

import org.springframework.http.ResponseEntity;

import java.util.function.Supplier;

public interface IdempotencyService {

    <T> ResponseEntity<T> execute(String idempotencyKey, Class<T> responseType, Supplier<ResponseEntity<T>> action);
}
