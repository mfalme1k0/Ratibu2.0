package com.ratibu.common.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * A globally unique identifier that can be generated on a client device
 * without any server round trip, per ADR-010 section 14.
 *
 * <p>Every synchronizable entity (bookings, payments, expenses, clients,
 * EOD reconciliations, and so on) is identified by a StableId. This is
 * what makes offline creation and later idempotent synchronization possible
 * (ADR-010 section 13): the server treats an incoming operation as a no-op
 * if it has already accepted an operation with the same StableId.
 */
public final class StableId {

    private final UUID value;

    private StableId(UUID value) {
        this.value = value;
    }

    /** Generates a new StableId. Safe to call offline, on any device. */
    public static StableId generate() {
        return new StableId(UUID.randomUUID());
    }

    /** Reconstructs a StableId from its canonical string form (e.g. from a client payload). */
    public static StableId fromString(String value) {
        return new StableId(UUID.fromString(value));
    }

    public UUID asUuid() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StableId stableId)) return false;
        return value.equals(stableId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}