package com.loan.loanapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.loanapi.entity.IdempotencyKey;
import com.loan.loanapi.repository.IdempotencyKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceImplTest {

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    private IdempotencyServiceImpl idempotencyService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        idempotencyService = new IdempotencyServiceImpl(idempotencyKeyRepository, objectMapper);
    }

    @Test
    void firstCall_executesActionAndPersistsResponse() {
        when(idempotencyKeyRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        AtomicInteger invocations = new AtomicInteger();

        ResponseEntity<Map<String, Object>> response = idempotencyService.execute("key-1",
                (Class<Map<String, Object>>) (Class<?>) Map.class,
                () -> {
                    invocations.incrementAndGet();
                    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("userId", "Bruce"));
                });

        assertThat(invocations.get()).isEqualTo(1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(idempotencyKeyRepository).save(any(IdempotencyKey.class));
    }

    @Test
    void replayedCall_doesNotReexecuteAction() {
        IdempotencyKey stored = IdempotencyKey.builder()
                .id(UUID.randomUUID())
                .idempotencyKey("key-1")
                .statusCode(201)
                .responseBody("{\"userId\":\"Bruce\"}")
                .createdAt(Instant.now())
                .build();
        when(idempotencyKeyRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(stored));
        AtomicInteger invocations = new AtomicInteger();

        ResponseEntity<Map<String, Object>> response = idempotencyService.execute("key-1",
                (Class<Map<String, Object>>) (Class<?>) Map.class,
                () -> {
                    invocations.incrementAndGet();
                    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("userId", "Bruce"));
                });

        assertThat(invocations.get()).isZero();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("userId", "Bruce");
        verify(idempotencyKeyRepository, never()).save(any());
    }
}
