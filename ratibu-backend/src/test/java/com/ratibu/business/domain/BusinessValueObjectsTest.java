package com.ratibu.business.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the parts of the value objects the use-case tests never touch:
 * hashCode, toString, and equals against null / other types.
 */
class BusinessValueObjectsTest {

    // ------------------------------------------------------------------ BusinessName

    @Test
    void businessName_toString_isTheTrimmedValue() {
        assertEquals("Shop", BusinessName.of("  Shop  ").toString());
    }

    @Test
    void businessName_hashCode_followsEquality() {
        assertEquals(BusinessName.of("A").hashCode(), BusinessName.of(" A ").hashCode());
    }

    @Test
    @SuppressWarnings({"EqualsWithItself", "ConstantValue", "SimplifiableAssertion"})
    void businessName_equals_isFalseForNullAndOtherTypes_trueForItself() {
        BusinessName name = BusinessName.of("A");

        assertTrue(name.equals(name));
        assertFalse(name.equals(null));
        assertFalse(name.equals("A"));
    }

    // ------------------------------------------------------------------ CurrencyCode

    @Test
    void currencyCode_toString_isTheNormalisedCode() {
        assertEquals("KES", CurrencyCode.of(" kes ").toString());
    }

    @Test
    void currencyCode_hashCode_followsEquality() {
        assertEquals(CurrencyCode.of("KES").hashCode(), CurrencyCode.of("kes").hashCode());
    }

    @Test
    @SuppressWarnings({"EqualsWithItself", "ConstantValue", "SimplifiableAssertion"})
    void currencyCode_equals_isFalseForNullAndOtherTypes_trueForItself() {
        CurrencyCode code = CurrencyCode.of("KES");

        assertTrue(code.equals(code));
        assertFalse(code.equals(null));
        assertFalse(code.equals("KES"));
    }
}