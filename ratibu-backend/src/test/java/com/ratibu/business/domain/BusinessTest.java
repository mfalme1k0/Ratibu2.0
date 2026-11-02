package com.ratibu.business.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BusinessTest {

    @Test
    void create_assignsStableId_andRaisesBusinessCreated() {
        Business business = Business.create(
                BusinessName.of("John's Barbershop"),
                CurrencyCode.of("KES"));

        assertNotNull(business.getId());
        assertEquals("John's Barbershop", business.getName().asString());
        assertEquals("KES", business.getCurrency().asString());
        assertNotNull(business.getCreatedAt());

        List<DomainEvent> events = business.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(BusinessCreated.class, events.getFirst());

        BusinessCreated created = (BusinessCreated) events.getFirst();
        assertEquals(business.getId(), created.businessId());
        assertEquals(business.getName(), created.name());
        assertEquals(business.getCurrency(), created.currency());
        assertEquals(business.getCreatedAt(), created.occurredAt());

        // Events are drained
        assertTrue(business.pullDomainEvents().isEmpty());
    }

    @Test
    void create_withExplicitId_preservesIdAndTimestamp() {
        StableId id = StableId.fromString("11111111-1111-1111-1111-111111111111");
        Instant at = Instant.parse("2026-01-15T10:00:00Z");

        Business business = Business.create(
                id,
                BusinessName.of("Mary's Salon"),
                CurrencyCode.of("USD"),
                at);

        assertEquals(id, business.getId());
        assertEquals(at, business.getCreatedAt());
        assertEquals(1, business.pullDomainEvents().size());
    }

    @Test
    void reconstitute_doesNotRaiseEvents() {
        StableId id = StableId.generate();
        Business business = Business.reconstitute(
                id,
                BusinessName.of("Fidel Auto Services"),
                CurrencyCode.of("KES"),
                Instant.now());

        assertEquals(id, business.getId());
        assertTrue(business.pullDomainEvents().isEmpty());
    }

    @Test
    void rename_updatesName() {
        Business business = Business.create(
                BusinessName.of("Old Name"),
                CurrencyCode.of("KES"));
        business.pullDomainEvents(); // drain create event

        business.rename(BusinessName.of("New Name"));
        assertEquals("New Name", business.getName().asString());
    }

    @Test
    void rename_rejectsNull() {
        Business business = Business.create(
                BusinessName.of("Name"),
                CurrencyCode.of("KES"));

        assertThrows(InvariantViolationException.class, () -> business.rename(null));
    }
}
