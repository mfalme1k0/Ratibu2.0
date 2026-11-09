-- Catalog Service aggregate (ADR-004 §14–§19)
-- Business-level service definition with default price.
-- Shop-specific availability/pricing deferred to a later migration.

CREATE TABLE catalog_service (
    id             UUID                        PRIMARY KEY,
    business_id    UUID                        NOT NULL,
    name           VARCHAR(120)                NOT NULL,
    description    VARCHAR(2000),
    default_price  NUMERIC(19, 2)              NOT NULL,
    status         VARCHAR(16)                 NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT fk_catalog_service_business
        FOREIGN KEY (business_id) REFERENCES business (id) ON DELETE CASCADE,
    CONSTRAINT chk_catalog_service_default_price_non_negative
        CHECK (default_price >= 0)
);

CREATE INDEX idx_catalog_service_business_id ON catalog_service (business_id);
