-- Client aggregate (ADR-004 §3–§7, ADR-013 §7)
-- Business-wide customer identity. Phone is a soft matching key only (no unique constraint).

CREATE TABLE client (
    id           UUID                        PRIMARY KEY,
    business_id  UUID                        NOT NULL,
    name         VARCHAR(120)                NOT NULL,
    phone        VARCHAR(32),
    email        VARCHAR(254),
    notes        VARCHAR(2000),
    status       VARCHAR(16)                 NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT fk_client_business
        FOREIGN KEY (business_id) REFERENCES business (id) ON DELETE CASCADE
);

CREATE INDEX idx_client_business_id ON client (business_id);
CREATE INDEX idx_client_business_phone ON client (business_id, phone);
