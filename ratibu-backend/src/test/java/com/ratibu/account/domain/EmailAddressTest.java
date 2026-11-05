package com.ratibu.account.domain;

import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailAddressTest {

    @Test
    void of_normalizesToLowerCase() {
        assertEquals("jane@example.com", EmailAddress.of("  Jane@Example.COM ").asString());
    }

    @Test
    void of_rejectsInvalid() {
        assertThrows(InvariantViolationException.class, () -> EmailAddress.of("not-an-email"));
        assertThrows(InvariantViolationException.class, () -> EmailAddress.of(""));
        assertThrows(InvariantViolationException.class, () -> EmailAddress.of(null));
    }
}
