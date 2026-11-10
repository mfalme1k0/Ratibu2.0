-- Booking aggregate (ADR-005, ADR-013 §4–§6)
-- Operational lifecycle entity; independent of Payment.
-- expected_price is a snapshot at create time.
-- ON DELETE CASCADE aligns with business/shop teardown; domain prefers soft-close over hard delete.

CREATE TABLE booking (
    id                 UUID                        PRIMARY KEY,
    shop_id            UUID                        NOT NULL,
    client_id          UUID                        NOT NULL,
    service_id         UUID                        NOT NULL,
    worker_id          UUID,
    scheduled_at       TIMESTAMP WITH TIME ZONE    NOT NULL,
    duration_minutes   INTEGER,
    expected_price     NUMERIC(19, 2)              NOT NULL,
    final_price        NUMERIC(19, 2),
    status             VARCHAR(16)                 NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE    NOT NULL,
    CONSTRAINT fk_booking_shop
        FOREIGN KEY (shop_id) REFERENCES shop (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_client
        FOREIGN KEY (client_id) REFERENCES client (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_service
        FOREIGN KEY (service_id) REFERENCES catalog_service (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_worker
        FOREIGN KEY (worker_id) REFERENCES worker (id) ON DELETE SET NULL,
    CONSTRAINT chk_booking_expected_price_non_negative
        CHECK (expected_price >= 0),
    CONSTRAINT chk_booking_final_price_non_negative
        CHECK (final_price IS NULL OR final_price >= 0),
    CONSTRAINT chk_booking_duration_positive
        CHECK (duration_minutes IS NULL OR duration_minutes > 0)
);

CREATE INDEX idx_booking_shop_id ON booking (shop_id);
CREATE INDEX idx_booking_client_id ON booking (client_id);
CREATE INDEX idx_booking_service_id ON booking (service_id);
CREATE INDEX idx_booking_scheduled_at ON booking (scheduled_at);
CREATE INDEX idx_booking_status ON booking (status);
