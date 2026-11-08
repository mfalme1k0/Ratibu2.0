package com.ratibu.client.domain;

import com.ratibu.common.exception.InvariantViolationException;

import java.util.Objects;

/**
 * Display name of a Client. Identity is {@link com.ratibu.common.domain.StableId};
 * the name is mutable and is not a unique key (ADR-004 §4–§5).
 */
public final class ClientName {

    public static final int MAX_LENGTH = 120;

    private final String value;

    private ClientName(String value) {
        this.value = value;
    }

    public static ClientName of(String raw) {
        if (raw == null) {
            throw new InvariantViolationException("Client name must not be null");
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvariantViolationException("Client name must not be blank");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvariantViolationException(
                    "Client name must be at most " + MAX_LENGTH + " characters");
        }
        return new ClientName(trimmed);
    }

    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientName that)) return false;
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
