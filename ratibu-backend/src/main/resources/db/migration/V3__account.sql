-- User Account aggregate (ADR-002 §4)
-- Email is a unique business key for login lookup; id is client-safe StableId.

CREATE TABLE account (
    id            UUID                        PRIMARY KEY,
    email         VARCHAR(254)                NOT NULL,
    display_name  VARCHAR(120),
    created_at    TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT uq_account_email UNIQUE (email)
);

CREATE INDEX idx_account_created_at ON account (created_at);
