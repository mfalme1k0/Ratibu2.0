package com.ratibu.worker.domain;

import com.ratibu.common.exception.InvariantViolationException;

import java.util.Objects;

/**
 * Display name of a Worker Profile. Identity is {@link com.ratibu.common.domain.StableId};
 * the name is mutable and is not a unique key (ADR-004 §8).
 */
public final class WorkerName {

    public static final int MAX_LENGTH = 120;

    private final String value;

    private WorkerName(String value) {
        this.value = value;
    }

    public static WorkerName of(String raw) {
        if (raw == null) {
            throw new InvariantViolationException("Worker name must not be null");
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvariantViolationException("Worker name must not be blank");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvariantViolationException(
                    "Worker name must be at most " + MAX_LENGTH + " characters");
        }
        return new WorkerName(trimmed);
    }

    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerName that)) return false;
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
