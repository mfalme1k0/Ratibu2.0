package com.ratibu.account.domain;

import com.ratibu.common.exception.InvariantViolationException;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Login email for a User Account (ADR-002 §4). Stored normalized to lower case.
 * Identity of the account remains {@link com.ratibu.common.domain.StableId};
 * email is a unique business key for lookup, not the aggregate id.
 */
public final class EmailAddress {

    public static final int MAX_LENGTH = 254;

    private static final Pattern BASIC = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String value;

    private EmailAddress(String value) {
        this.value = value;
    }

    public static EmailAddress of(String raw) {
        if (raw == null) {
            throw new InvariantViolationException("Email must not be null");
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new InvariantViolationException("Email must not be blank");
        }
        if (normalized.length() > MAX_LENGTH) {
            throw new InvariantViolationException("Email must be at most " + MAX_LENGTH + " characters");
        }
        if (!BASIC.matcher(normalized).matches()) {
            throw new InvariantViolationException("Email format is invalid: " + raw);
        }
        return new EmailAddress(normalized);
    }

    public String asString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailAddress that)) return false;
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
