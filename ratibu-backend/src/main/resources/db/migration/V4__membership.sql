-- Business Membership (ADR-002 §5–6)
-- One membership per (account, business). Role is OWNER | MANAGER | WORKER.

CREATE TABLE membership (
    id           UUID                        PRIMARY KEY,
    account_id   UUID                        NOT NULL,
    business_id  UUID                        NOT NULL,
    role         VARCHAR(16)                 NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT fk_membership_account
        FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT fk_membership_business
        FOREIGN KEY (business_id) REFERENCES business (id) ON DELETE CASCADE,
    CONSTRAINT uq_membership_account_business UNIQUE (account_id, business_id)
);

CREATE INDEX idx_membership_business_id ON membership (business_id);
CREATE INDEX idx_membership_account_id ON membership (account_id);
