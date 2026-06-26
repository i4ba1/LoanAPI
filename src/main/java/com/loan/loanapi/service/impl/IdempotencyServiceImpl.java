package com.loan.loanapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.loanapi.entity.IdempotencyKey;
import com.loan.loanapi.repository.IdempotencyKeyRepository;
import com.loan.loanapi.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Guards a write endpoint against duplicate execution when a client retries the same
 * logical request (e.g. after a timeout) with the same Idempotency-Key header.
 * First call executes the action and persists its response; replays return the stored
 * response without re-executing the action.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public <T> ResponseEntity<T> execute(String idempotencyKey, Class<T> responseType,
                                          Supplier<ResponseEntity<T>> action) {
        var existing = idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotency-Key {} already processed, replaying stored response", idempotencyKey);
            return replay(existing.get(), responseType);
        }

        ResponseEntity<T> response = action.get();

        try {
            persist(idempotencyKey, response);
        } catch (DataIntegrityViolationException e) {
            log.warn("Idempotency-Key {} was persisted by a concurrent request, replaying its response",
                    idempotencyKey);
            return replay(idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> e), responseType);
        }

        return response;
    }

    // Relies on SimpleJpaRepository#save being independently @Transactional - no
    // surrounding transaction needs to span this call.
    private void persist(String idempotencyKey, ResponseEntity<?> response) {
        try {
            IdempotencyKey record = IdempotencyKey.builder()
                    .idempotencyKey(idempotencyKey)
                    .statusCode(response.getStatusCode().value())
                    .responseBody(objectMapper.writeValueAsString(response.getBody()))
                    .build();
            idempotencyKeyRepository.save(record);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize response for idempotency storage", e);
        }
    }

    private <T> ResponseEntity<T> replay(IdempotencyKey record, Class<T> responseType) {
        try {
            T body = objectMapper.readValue(record.getResponseBody(), responseType);
            return ResponseEntity.status(HttpStatus.valueOf(record.getStatusCode())).body(body);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize stored idempotent response", e);
        }
    }
}
