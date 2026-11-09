package com.ratibu.catalog.domain;

import com.ratibu.common.exception.InvariantViolationException;

import java.util.Objects;

/**
 * Display name of a business-level service (ADR-004 §14). Not a unique key.
 */
public final class ServiceName {

    public static final int MAX_LENGTH = 120;

    private final String value;

    private ServiceName(String value) {
        this.value = value;
    }

    public static ServiceName of(String raw) {
        if (raw == null) {
            throw new InvariantViolationException("Service name must not be null");
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvariantViolationException("Service name must not be blank");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvariantViolationException(
                    "Service name must be at most " + MAX_LENGTH + " characters");
        }
        return new ServiceName(trimmed);
    }

    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceName that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
