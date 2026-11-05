package com.ratibu.common.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StableIdTest {

    private static final String CANONICAL = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

    @Test
    void generate_producesDistinctRandomUuids() {
        StableId first = StableId.generate();
        StableId second = StableId.generate();

        assertNotNull(first.asUuid());
        assertNotEquals(first, second);
        assertEquals(7, first.asUuid().version());
    }

    @Test
    void fromString_parsesTheCanonicalForm() {
        StableId id = StableId.fromString(CANONICAL);

        assertEquals(UUID.fromString(CANONICAL), id.asUuid());
        assertEquals(CANONICAL, id.toString());
    }

    @Test
    void fromString_upperCaseInput_isNormalisedToLowerCase() {
        assertEquals(CANONICAL, StableId.fromString(CANONICAL.toUpperCase()).toString());
    }

    @Test
    void fromString_rejectsMalformedInput() {
        assertThrows(IllegalArgumentException.class, () -> StableId.fromString("not-a-uuid"));
    }

    @Test
    void fromUuid_wrapsTheUuid() {
        UUID uuid = UUID.randomUUID();

        assertSame(uuid, StableId.fromUuid(uuid).asUuid());
    }

    @Test
    void fromUuid_rejectsNull() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> StableId.fromUuid(null));
        assertEquals("uuid must not be null", ex.getMessage());
    }

    @Test
    @SuppressWarnings({"EqualsWithItself", "ConstantValue", "SimplifiableAssertion"})
    void equals_coversIdentityValueDifferenceNullAndOtherTypes() {
        StableId id = StableId.fromString(CANONICAL);

        assertTrue(id.equals(id));
        assertTrue(id.equals(StableId.fromString(CANONICAL)));
        assertFalse(id.equals(StableId.generate()));
        assertFalse(id.equals(null));
        assertFalse(id.equals(CANONICAL));
        assertFalse(id.equals(UUID.fromString(CANONICAL)));
    }

    @Test
    void hashCode_isConsistentWithEquals() {
        assertEquals(StableId.fromString(CANONICAL).hashCode(), StableId.fromString(CANONICAL).hashCode());
    }
}