package com.ratibu.business.application;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateBusinessCommandTest {

    @Test
    void canonicalConstructor_withNullBusinessId_normalisesToEmpty() {
        CreateBusinessCommand command = new CreateBusinessCommand(null, "Shop", "KES");

        assertTrue(command.businessId().isEmpty());
    }

    @Test
    void canonicalConstructor_keepsAPresentBusinessId() {
        CreateBusinessCommand command = new CreateBusinessCommand(Optional.of("abc"), "Shop", "KES");

        assertEquals(Optional.of("abc"), command.businessId());
        assertEquals("Shop", command.name());
        assertEquals("KES", command.currency());
    }

    @Test
    void of_withoutId_hasNoBusinessId() {
        assertTrue(CreateBusinessCommand.of("Shop", "KES").businessId().isEmpty());
    }

    @Test
    void of_withId_carriesIt() {
        assertEquals(Optional.of("abc"), CreateBusinessCommand.of("abc", "Shop", "KES").businessId());
    }

    @Test
    void of_withNullId_hasNoBusinessId() {
        assertTrue(CreateBusinessCommand.of(null, "Shop", "KES").businessId().isEmpty());
    }
}