package com.ratibu.client.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    private final StableId businessId = StableId.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final ClientName name = ClientName.of("Jane Doe");

    @Test
    void create_raisesClientCreated() {
        Client c = Client.create(businessId, name, null, null, null);

        assertEquals(name, c.getName());
        assertEquals(businessId, c.getBusinessId());
        assertTrue(c.getPhone().isEmpty());
        assertEquals(ClientStatus.ACTIVE, c.getStatus());

        List<DomainEvent> events = c.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(ClientCreated.class, events.getFirst());
    }

    @Test
    void create_withPhoneAndEmail() {
        Client c = Client.create(
                businessId, name, PhoneNumber.of("+254700000000"), "jane@example.com", "VIP");
        assertEquals("+254700000000", c.getPhone().orElseThrow().asString());
        assertEquals("jane@example.com", c.getEmail().orElseThrow());
        assertEquals("VIP", c.getNotes().orElseThrow());
    }

    @Test
    void reconstitute_doesNotRaiseEvents() {
        Client c = Client.reconstitute(
                StableId.generate(), businessId, name, null, null, null,
                ClientStatus.ACTIVE, Instant.now());
        assertTrue(c.pullDomainEvents().isEmpty());
    }

    @Test
    void rename_updatesName() {
        Client c = Client.create(businessId, name, null, null, null);
        c.pullDomainEvents();
        c.rename(ClientName.of("Jane K."));
        assertEquals("Jane K.", c.getName().asString());
    }

    @Test
    void rename_rejectsInactive() {
        Client c = Client.create(businessId, name, null, null, null);
        c.deactivate();
        assertThrows(InvariantViolationException.class,
                () -> c.rename(ClientName.of("Other")));
    }

    @Test
    void deactivate_isIdempotent() {
        Client c = Client.create(businessId, name, null, null, null);
        c.deactivate();
        c.deactivate();
        assertEquals(ClientStatus.INACTIVE, c.getStatus());
        assertFalse(c.isActive());
    }

    @Test
    void activate_restoresActive() {
        Client c = Client.create(businessId, name, null, null, null);
        c.deactivate();
        c.activate();
        assertTrue(c.isActive());
    }

    @Test
    void create_rejectsBlankName() {
        assertThrows(InvariantViolationException.class, () -> ClientName.of("  "));
    }

    @Test
    void phone_rejectsBlank() {
        assertThrows(InvariantViolationException.class, () -> PhoneNumber.of(" "));
    }
}
