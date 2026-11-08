package com.ratibu.client.domain;

import com.ratibu.common.exception.InvariantViolationException;

import java.util.Objects;

/**
 * Optional client phone. Soft matching key for the manual merge queue
 * (ADR-013 §7) — not a unique business key and never auto-merged.
 */
public final class PhoneNumber {

    public static final int MAX_LENGTH = 32;

    private final String value;

    private PhoneNumber(String value) {
        this.value = value;
    }

    public static PhoneNumber of(String raw) {
        if (raw == null) {
            throw new InvariantViolationException("Phone number must not be null");
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvariantViolationException("Phone number must not be blank");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvariantViolationException(
                    "Phone number must be at most " + MAX_LENGTH + " characters");
        }
        return new PhoneNumber(trimmed);
    }

    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber that)) return false;
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
