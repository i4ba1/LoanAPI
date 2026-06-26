CREATE TABLE idempotency_keys (
    id              UUID          NOT NULL,
    idempotency_key VARCHAR(255)  NOT NULL,
    response_body   TEXT,
    status_code     INTEGER,
    created_at      TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_idempotency_keys PRIMARY KEY (id),
    CONSTRAINT uq_idempotency_key  UNIQUE (idempotency_key)
);
