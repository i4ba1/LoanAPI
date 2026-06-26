# Loan API

REST API for vehicle loan applications built with Spring Boot 3 and Java 21.

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 3.5 |
| Persistence | Spring Data JPA, PostgreSQL |
| Schema Migration | Flyway |
| Validation | Jakarta Validation |
| Build | Maven |
| Testing | JUnit 5, Mockito, H2 (in-memory) |

---

## Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 14+ running on `localhost:5432`

---

## Database Setup

Create the database once:

```sql
CREATE DATABASE loan;
```

Flyway manages all table creation automatically on startup. No manual SQL required.

---

## Running the App

```bash
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**.

Environment variable overrides:

```bash
JWT_SECRET=your-secret-key-minimum-32-bytes ./mvnw spring-boot:run
```

---

## Running Tests

```bash
./mvnw test
```

Tests use an H2 in-memory database (Flyway disabled in test profile). No PostgreSQL needed to run tests.

---

## API Reference

All request and response bodies use **snake_case** field names.

### POST `/api/v1/loans` — Submit Loan Application

**Headers**

| Header | Required | Description |
|---|---|---|
| `Content-Type` | Yes | `application/json` |
| `Idempotency-Key` | No | UUID string; same key replays the original response without re-saving |

**Request Body**

```json
{
  "user_id": "Bruce",
  "mrp": 100000000,
  "dp": 20000000,
  "vehicle_year": 2022,
  "police_number": "B 1234 ABC",
  "machine_number": "SDR72V25000W201"
}
```

| Field | Type | Rules |
|---|---|---|
| `user_id` | string | required |
| `mrp` | number | required, positive |
| `dp` | number | required, positive, must be less than `mrp` |
| `vehicle_year` | integer | required, cannot be in the future |
| `police_number` | string | required |
| `machine_number` | string | required |

**Response `201 Created`**

```json
{
  "user_id": "Bruce",
  "loans": [
    {
      "mrp": 100000000,
      "dp": 20000000,
      "vehicle_year": 2022,
      "police_number": "B 1234 ABC",
      "machine_number": "SDR72V25000W201",
      "status": "SUBMITTED"
    }
  ]
}
```

---

### POST `/api/v1/loans/approval` — Approve Loan

**Request Body**

```json
{
  "user_id": "Bruce",
  "police_number": "B 1234 ABC"
}
```

Finds the most recent loan for the given `user_id` + `police_number`. Approves it if status is `SUBMITTED`.

**Response `200 OK`**

```json
{
  "user_id": "Bruce",
  "police_number": "B 1234 ABC",
  "message": "Loan updated successfully."
}
```

---

## Error Contract

All errors follow a consistent shape:

```json
{
  "error": "error_code",
  "error_description": "Human readable message."
}
```

| HTTP Status | `error` | Cause |
|---|---|---|
| `400` | `invalid_request` | Validation failure (missing field, dp ≥ mrp, future vehicle year) |
| `404` | `loan_not_found` | No loan exists for the given user + police number |
| `409` | `loan_already_processed` | Loan is already approved or rejected |
| `409` | `concurrent_update_conflict` | Optimistic lock conflict; client should retry |

---

## Key Design Decisions

### Idempotency
`POST /api/v1/loans` accepts an optional `Idempotency-Key` header. The key and its response are persisted in the `idempotency_keys` table. Retrying with the same key replays the stored response without saving a duplicate loan. Concurrent requests with the same key are handled via a unique constraint — the losing request replays the winner's response.

### Optimistic Locking
The `loans` table has a `version` column managed by Hibernate's `@Version`. Concurrent approve attempts on the same loan result in a `409 concurrent_update_conflict` rather than a silent overwrite.

### Validation
Business-rule validation (dp < mrp, vehicle year not in future) is enforced by a class-level `@ValidLoanRequest` constraint, keeping the rules co-located with the DTO.

### Correlation ID
Every request is assigned an `X-Correlation-Id` (generated if not supplied by the caller). It is propagated through MDC so all log lines for a single request share the same ID.

### Schema Migrations
Flyway is used for all schema changes. `ddl-auto` is set to `validate` — Hibernate never modifies the schema, Flyway owns it entirely.

---

## Project Structure

```
src/main/java/com/loan/loanapi/
├── controller/       # HTTP layer (LoanController, AuthController)
├── service/          # Business logic interfaces + implementations
├── repository/       # Spring Data JPA repositories
├── entity/           # JPA entities (Loan, IdempotencyKey)
├── dto/
│   ├── request/      # Inbound DTOs + validators
│   └── response/     # Outbound DTOs
├── exception/        # Custom exceptions + GlobalExceptionHandler
├── filter/           # CorrelationIdFilter (MDC)
├── security/         # JWT infrastructure (disabled — see note below)
└── enums/            # LoanStatus

src/main/resources/
├── application.yaml
└── db/migration/     # Flyway scripts (V0, V1, V2)
```

---

## Loan Status Flow

```
SUBMITTED  ──(approve)──▶  APPROVED
           ──(reject)───▶  REJECTED
```

Once a loan reaches `APPROVED` or `REJECTED`, it cannot be processed again (`409 loan_already_processed`).

---

## Notes

- **JWT / Spring Security** — infrastructure classes exist (`JwtService`, `SecurityConfig`, `JwtAuthenticationFilter`) but are currently disabled (commented out) pending full auth integration.
- **Production gaps** — rate limiting, distributed tracing, and a persistent user store are not implemented; these are documented in `INTERVIEW_PREP.md`.
