package com.ratibu.account.domain;

import com.ratibu.common.exception.InvariantViolationException;

import java.util.Objects;

/**
 * Optional human-readable name on a User Account (ADR-002 §4).
 */
public final class DisplayName {

    public static final int MAX_LENGTH = 120;

    private final String value;

    private DisplayName(String value) {
        this.value = value;
    }

    public static DisplayName of(String raw) {
        if (raw == null) {
            throw new InvariantViolationException("Display name must not be null");
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvariantViolationException("Display name must not be blank");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvariantViolationException(
                    "Display name must be at most " + MAX_LENGTH + " characters");
        }
        return new DisplayName(trimmed);
    }

    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DisplayName that)) return false;
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
