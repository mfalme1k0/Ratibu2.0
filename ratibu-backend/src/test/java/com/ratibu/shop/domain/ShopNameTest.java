package com.ratibu.shop.domain;

import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShopNameTest {

    @Test
    void of_trimsAndAcceptsValidName() {
        assertEquals("Westlands", ShopName.of("  Westlands  ").asString());
    }

    @Test
    void of_rejectsNull() {
        assertThrows(InvariantViolationException.class, () -> ShopName.of(null));
    }

    @Test
    void of_rejectsBlank() {
        assertThrows(InvariantViolationException.class, () -> ShopName.of("   "));
    }

    @Test
    void of_rejectsTooLong() {
        String tooLong = "x".repeat(ShopName.MAX_LENGTH + 1);
        assertThrows(InvariantViolationException.class, () -> ShopName.of(tooLong));
    }

    @Test
    void equality_isValueBased() {
        assertEquals(ShopName.of("A"), ShopName.of("A"));
        assertNotEquals(ShopName.of("A"), ShopName.of("B"));
    }
}
