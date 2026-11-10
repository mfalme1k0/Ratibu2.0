package com.ratibu.booking.domain;

/**
 * Booking lifecycle (ADR-005 §10, ADR-013 §8.3 correction).
 *
 * <pre>
 * SCHEDULED → IN_PROGRESS → COMPLETED
 *          ↘ CANCELLED
 *          ↘ NO_SHOW
 * </pre>
 */
public enum BookingStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
