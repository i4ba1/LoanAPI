CREATE TABLE loans (
    id            UUID                     NOT NULL,
    user_id       VARCHAR(255)             NOT NULL,
    mrp           NUMERIC(38, 2)           NOT NULL,
    dp            NUMERIC(38, 2)           NOT NULL,
    vehicle_year  INTEGER                  NOT NULL,
    police_number VARCHAR(255)             NOT NULL,
    machine_number VARCHAR(255)            NOT NULL,
    status        VARCHAR(50)              NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE,
    updated_at    TIMESTAMP WITH TIME ZONE,
    version       BIGINT,
    CONSTRAINT pk_loans PRIMARY KEY (id)
);
