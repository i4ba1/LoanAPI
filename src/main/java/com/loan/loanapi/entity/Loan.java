package com.loan.loanapi.entity;

import com.loan.loanapi.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue
    private UUID id;

    private String userId;

    private BigDecimal mrp;

    private BigDecimal dp;

    private Integer vehicleYear;

    @Column(nullable = false)
    private String policeNumber;

    private String machineNumber;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    @Version
    private Long version;

    @jakarta.persistence.PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @jakarta.persistence.PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
