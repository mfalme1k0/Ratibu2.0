package com.ratibu.business.domain;

import com.ratibu.common.exception.InvariantViolationException;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * ISO 4217 currency code for a Business. One currency per Business in MVP
 * (ADR-011 §49). Multi-currency support is explicitly deferred.
 *
 * <p>The allowed set is intentionally small and can be expanded without
 * changing the aggregate API. Codes are stored and compared in upper case.
 */
public final class CurrencyCode {

    /** Currencies accepted at Business creation in the current MVP. */
    private static final Set<String> SUPPORTED = Set.of(
            "KES", // Kenyan Shilling — primary target market
            "USD",
            "EUR",
            "GBP",
            "UGX",
            "TZS",
            "RWF"
    );

    private final String code;

    private CurrencyCode(String code) {
        this.code = code;
    }

    public static CurrencyCode of(String raw) {
        if (raw == null) {
            throw new InvariantViolationException("Currency code must not be null");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() != 3) {
            throw new InvariantViolationException(
                    "Currency code must be a 3-letter ISO 4217 code, got: " + raw);
        }
        if (!SUPPORTED.contains(normalized)) {
            throw new InvariantViolationException(
                    "Currency code is not supported in MVP: " + normalized
                            + ". Supported: " + SUPPORTED);
        }
        return new CurrencyCode(normalized);
    }

    public String asString() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurrencyCode that)) return false;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
