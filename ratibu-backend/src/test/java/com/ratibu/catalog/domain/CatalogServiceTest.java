package com.ratibu.catalog.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogServiceTest {

    private final StableId businessId = StableId.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final ServiceName name = ServiceName.of("Haircut");
    private final Money price = Money.of(new BigDecimal("500.00"));

    @Test
    void create_raisesCatalogServiceCreated() {
        CatalogService s = CatalogService.create(businessId, name, null, price);

        assertEquals(name, s.getName());
        assertEquals(businessId, s.getBusinessId());
        assertEquals(price, s.getDefaultPrice());
        assertEquals(ServiceStatus.ACTIVE, s.getStatus());

        List<DomainEvent> events = s.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(CatalogServiceCreated.class, events.getFirst());
    }

    @Test
    void create_rejectsNegativePrice() {
        assertThrows(InvariantViolationException.class,
                () -> CatalogService.create(businessId, name, null, Money.of(new BigDecimal("-1"))));
    }

    @Test
    void reconstitute_doesNotRaiseEvents() {
        CatalogService s = CatalogService.reconstitute(
                StableId.generate(), businessId, name, null, price,
                ServiceStatus.ACTIVE, Instant.now());
        assertTrue(s.pullDomainEvents().isEmpty());
    }

    @Test
    void changeDefaultPrice_updatesPrice() {
        CatalogService s = CatalogService.create(businessId, name, null, price);
        s.pullDomainEvents();
        s.changeDefaultPrice(Money.of(new BigDecimal("600.00")));
        assertEquals(Money.of(new BigDecimal("600.00")), s.getDefaultPrice());
    }

    @Test
    void discontinue_isIdempotent() {
        CatalogService s = CatalogService.create(businessId, name, null, price);
        s.discontinue();
        s.discontinue();
        assertEquals(ServiceStatus.DISCONTINUED, s.getStatus());
        assertFalse(s.isActive());
    }

    @Test
    void rename_rejectsDiscontinued() {
        CatalogService s = CatalogService.create(businessId, name, null, price);
        s.discontinue();
        assertThrows(InvariantViolationException.class,
                () -> s.rename(ServiceName.of("Other")));
    }

    @Test
    void serviceName_rejectsBlank() {
        assertThrows(InvariantViolationException.class, () -> ServiceName.of("  "));
    }
}
