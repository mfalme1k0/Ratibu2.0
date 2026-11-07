-- Worker Profile aggregate (ADR-004 §8–§13, ADR-009, ADR-013 §8.6)
-- Belongs to a Business. Optional link to a Business Membership (same business).
-- Shop assignments and service capabilities are deferred.

CREATE TABLE worker (
    id             UUID                        PRIMARY KEY,
    business_id    UUID                        NOT NULL,
    name           VARCHAR(120)                NOT NULL,
    membership_id  UUID,
    status         VARCHAR(16)                 NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT fk_worker_business
        FOREIGN KEY (business_id) REFERENCES business (id) ON DELETE CASCADE,
    CONSTRAINT fk_worker_membership
        FOREIGN KEY (membership_id) REFERENCES membership (id),
    CONSTRAINT uq_worker_membership UNIQUE (membership_id)
);

CREATE INDEX idx_worker_business_id ON worker (business_id);
CREATE INDEX idx_worker_membership_id ON worker (membership_id);
