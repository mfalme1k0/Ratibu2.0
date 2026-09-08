package com.ratibu.common.domain;

import java.time.Instant;

/**
 * Marker for something that happened within an aggregate that other parts
 * of the system may need to react to (e.g. BookingCompleted, PaymentRecorded,
 * EodReconciliationSubmitted). Aggregates raise these rather than calling
 * other aggregates' code directly - see {@link AggregateRoot} and ADR-012
 * section 19.
 */
public interface DomainEvent {
    Instant occurredAt();
}