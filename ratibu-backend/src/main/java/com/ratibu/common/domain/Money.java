package com.ratibu.common.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An exact monetary amount in a single currency. Backed by {@link BigDecimal},
 * never floating point, per ADR-011 section 50.
 *
 * <p>Currency is associated with the Business (ADR-011 section 49), not
 * carried per-Money-instance in the initial single-currency-per-Business
 * model. This type intentionally has no arithmetic operators that silently
 * mix currencies once multi-currency support is introduced later.
 */
public final class Money {

    private static final int SCALE = 2;

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        return new Money(amount);
    }

    public static Money of(long wholeUnits) {
        return new Money(BigDecimal.valueOf(wholeUnits));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isNegative() {
        return this.amount.signum() < 0;
    }

    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    public BigDecimal asBigDecimal() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}