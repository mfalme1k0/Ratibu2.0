package com.ratibu.shop.domain;

import com.ratibu.common.exception.InvariantViolationException;

import java.util.Objects;

/**
 * Display name of a Shop. Identity is {@link com.ratibu.common.domain.StableId};
 * the name is mutable and must never be treated as a unique key (ADR-003 §15).
 */
public final class ShopName {

    public static final int MAX_LENGTH = 120;

    private final String value;

    private ShopName(String value) {
        this.value = value;
    }

    public static ShopName of(String raw) {
        if (raw == null) {
            throw new InvariantViolationException("Shop name must not be null");
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvariantViolationException("Shop name must not be blank");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvariantViolationException(
                    "Shop name must be at most " + MAX_LENGTH + " characters");
        }
        return new ShopName(trimmed);
    }

    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShopName that)) return false;
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
