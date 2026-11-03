package com.ratibu.common.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    // ------------------------------------------------------------------ construction

    @Test
    void of_bigDecimal_scalesToTwoDecimalPlaces() {
        assertEquals("10.50", Money.of(new BigDecimal("10.5")).toString());
        assertEquals("10.00", Money.of(new BigDecimal("10")).toString());
        assertEquals("1000.00", Money.of(new BigDecimal("1E+3")).toString());
    }

    @Test
    void of_bigDecimal_roundsHalfUp_awayFromZeroOnTies() {
        assertEquals("1.01", Money.of(new BigDecimal("1.005")).toString());
        assertEquals("-1.01", Money.of(new BigDecimal("-1.005")).toString());
        assertEquals("1.00", Money.of(new BigDecimal("1.004")).toString());
    }

    @Test
    void of_bigDecimal_rejectsNull() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> Money.of((BigDecimal) null));
        assertEquals("amount must not be null", ex.getMessage());
    }

    @Test
    void of_wholeUnits_buildsExactAmounts() {
        assertEquals("5.00", Money.of(5L).toString());
        assertEquals("-3.00", Money.of(-3L).toString());
    }

    @Test
    void zero_isZeroAndNotNegative() {
        Money zero = Money.zero();

        assertTrue(zero.isZero());
        assertFalse(zero.isNegative());
        assertEquals("0.00", zero.toString());
    }

    // ------------------------------------------------------------------ arithmetic

    @Test
    void add_returnsANewMoney_leavingOperandsUntouched() {
        Money a = Money.of(new BigDecimal("10.25"));
        Money b = Money.of(new BigDecimal("0.75"));

        Money sum = a.add(b);

        assertEquals("11.00", sum.toString());
        assertEquals("10.25", a.toString());
        assertEquals("0.75", b.toString());
    }

    @Test
    void subtract_canGoNegative() {
        Money result = Money.of(1L).subtract(Money.of(new BigDecimal("2.50")));

        assertEquals("-1.50", result.toString());
        assertTrue(result.isNegative());
    }

    // ------------------------------------------------------------------ comparisons

    @Test
    void isGreaterThan_coversGreaterEqualAndSmaller() {
        assertTrue(Money.of(2L).isGreaterThan(Money.of(1L)));
        assertFalse(Money.of(1L).isGreaterThan(Money.of(1L)));
        assertFalse(Money.of(1L).isGreaterThan(Money.of(2L)));
    }

    @Test
    void isNegative_coversNegativeZeroAndPositive() {
        assertTrue(Money.of(-1L).isNegative());
        assertFalse(Money.zero().isNegative());
        assertFalse(Money.of(1L).isNegative());
    }

    @Test
    void isZero_coversZeroRoundedToZeroAndNonZero() {
        assertTrue(Money.zero().isZero());
        assertTrue(Money.of(new BigDecimal("0.004")).isZero()); // rounds to 0.00
        assertFalse(Money.of(new BigDecimal("0.01")).isZero());
        assertFalse(Money.of(-1L).isZero());
    }

    @Test
    void asBigDecimal_exposesTheTwoDecimalAmount() {
        BigDecimal amount = Money.of(1L).asBigDecimal();

        assertEquals(2, amount.scale());
        assertEquals(new BigDecimal("1.00"), amount);
    }

    // ------------------------------------------------------------------ equality

    @Test
    @SuppressWarnings({"EqualsWithItself", "ConstantValue", "SimplifiableAssertion"})
    void equals_coversIdentityValueDifferenceNullAndOtherTypes() {
        Money money = Money.of(10L);

        assertTrue(money.equals(money));
        assertTrue(money.equals(Money.of(new BigDecimal("10.00"))));
        assertTrue(Money.of(new BigDecimal("10.001")).equals(money)); // rounds to the same amount
        assertFalse(money.equals(Money.of(11L)));
        assertFalse(money.equals(null));
        assertFalse(money.equals("10.00"));
        assertFalse(money.equals(new BigDecimal("10.00")));
    }

    @Test
    void hashCode_isConsistentWithEquals() {
        assertEquals(Money.of(10L).hashCode(), Money.of(new BigDecimal("10.00")).hashCode());
        assertEquals(Money.zero().hashCode(), Money.of(new BigDecimal("0.001")).hashCode());
    }

    @Test
    void toString_isPlainNotScientific() {
        assertEquals("1234.50", Money.of(new BigDecimal("1234.5")).toString());
    }
}