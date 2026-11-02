package com.ratibu.business.domain;

import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessNameTest {

    @Test
    void of_trimsAndAcceptsValidName() {
        BusinessName name = BusinessName.of("  John's Barbershop  ");
        assertEquals("John's Barbershop", name.asString());
    }

    @Test
    void of_rejectsNull() {
        assertThrows(InvariantViolationException.class, () -> BusinessName.of(null));
    }

    @Test
    void of_rejectsBlank() {
        assertThrows(InvariantViolationException.class, () -> BusinessName.of("   "));
        assertThrows(InvariantViolationException.class, () -> BusinessName.of(""));
    }

    @Test
    void of_rejectsTooLong() {
        String tooLong = "x".repeat(BusinessName.MAX_LENGTH + 1);
        assertThrows(InvariantViolationException.class, () -> BusinessName.of(tooLong));
    }

    @Test
    void of_acceptsMaxLength() {
        String max = "x".repeat(BusinessName.MAX_LENGTH);
        assertEquals(max, BusinessName.of(max).asString());
    }

    @Test
    void equality_isValueBased() {
        assertEquals(BusinessName.of("A"), BusinessName.of("A"));
        assertNotEquals(BusinessName.of("A"), BusinessName.of("B"));
    }
}
