-- Business aggregate (ADR-003, ADR-012 §3.1, ADR-011 §49)
-- Identity is a client-generated UUID (StableId / ADR-010 §14).
-- TIMESTAMP WITH TIME ZONE is portable across PostgreSQL and H2 (PostgreSQL mode).

CREATE TABLE business (
    id          UUID                        PRIMARY KEY,
    name        VARCHAR(120)                NOT NULL,
    currency    VARCHAR(3)                  NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE    NOT NULL
);

CREATE INDEX idx_business_created_at ON business (created_at);
