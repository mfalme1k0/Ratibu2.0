package com.ratibu.business.domain;

import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyCodeTest {

    @Test
    void of_normalizesToUpperCase() {
        assertEquals("KES", CurrencyCode.of("kes").asString());
        assertEquals("USD", CurrencyCode.of(" Usd ").asString());
    }

    @Test
    void of_acceptsSupportedCodes() {
        assertDoesNotThrow(() -> CurrencyCode.of("KES"));
        assertDoesNotThrow(() -> CurrencyCode.of("USD"));
        assertDoesNotThrow(() -> CurrencyCode.of("EUR"));
        assertDoesNotThrow(() -> CurrencyCode.of("GBP"));
        assertDoesNotThrow(() -> CurrencyCode.of("UGX"));
        assertDoesNotThrow(() -> CurrencyCode.of("TZS"));
        assertDoesNotThrow(() -> CurrencyCode.of("RWF"));
    }

    @Test
    void of_rejectsNull() {
        assertThrows(InvariantViolationException.class, () -> CurrencyCode.of(null));
    }

    @Test
    void of_rejectsWrongLength() {
        assertThrows(InvariantViolationException.class, () -> CurrencyCode.of("KE"));
        assertThrows(InvariantViolationException.class, () -> CurrencyCode.of("KESX"));
    }

    @Test
    void of_rejectsUnsupported() {
        assertThrows(InvariantViolationException.class, () -> CurrencyCode.of("JPY"));
        assertThrows(InvariantViolationException.class, () -> CurrencyCode.of("XXX"));
    }

    @Test
    void equality_isValueBased() {
        assertEquals(CurrencyCode.of("KES"), CurrencyCode.of("kes"));
        assertNotEquals(CurrencyCode.of("KES"), CurrencyCode.of("USD"));
    }
}
