-- Shop aggregate (ADR-003, ADR-012 §4 / §16)
-- business_id references business.id. ON DELETE CASCADE keeps cleanup safe when
-- a Business is removed (shops cannot exist without their Business).

CREATE TABLE shop (
    id           UUID                        PRIMARY KEY,
    business_id  UUID                        NOT NULL,
    name         VARCHAR(120)                NOT NULL,
    status       VARCHAR(16)                 NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT fk_shop_business
        FOREIGN KEY (business_id) REFERENCES business (id) ON DELETE CASCADE
);

CREATE INDEX idx_shop_business_id ON shop (business_id);
CREATE INDEX idx_shop_created_at ON shop (created_at);
