package com.ratibu.business.domain;

import com.ratibu.common.exception.InvariantViolationException;

import java.util.Objects;

/**
 * Display name of a Business. Identity is carried by {@link com.ratibu.common.domain.StableId};
 * the name is mutable and must never be treated as a unique key (ADR-003 §15 pattern
 * applied at business level: rename must not break historical references).
 */
public final class BusinessName {

    public static final int MAX_LENGTH = 120;

    private final String value;

    private BusinessName(String value) {
        this.value = value;
    }

    public static BusinessName of(String raw) {
        if (raw == null) {
            throw new InvariantViolationException("Business name must not be null");
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvariantViolationException("Business name must not be blank");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvariantViolationException(
                    "Business name must be at most " + MAX_LENGTH + " characters");
        }
        return new BusinessName(trimmed);
    }

    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessName that)) return false;
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
