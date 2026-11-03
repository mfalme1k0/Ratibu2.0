package com.ratibu.shop.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShopTest {

    private final StableId businessId = StableId.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void create_assignsStableId_activeStatus_andRaisesShopCreated() {
        Shop shop = Shop.create(businessId, ShopName.of("Westlands"));

        assertNotNull(shop.getId());
        assertEquals(businessId, shop.getBusinessId());
        assertEquals("Westlands", shop.getName().asString());
        assertEquals(ShopStatus.ACTIVE, shop.getStatus());
        assertTrue(shop.isActive());
        assertNotNull(shop.getCreatedAt());

        List<DomainEvent> events = shop.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(ShopCreated.class, events.getFirst());

        ShopCreated created = (ShopCreated) events.getFirst();
        assertEquals(shop.getId(), created.shopId());
        assertEquals(businessId, created.businessId());
        assertEquals(shop.getName(), created.name());
        assertTrue(shop.pullDomainEvents().isEmpty());
    }

    @Test
    void create_withExplicitId_preservesIdAndTimestamp() {
        StableId id = StableId.fromString("11111111-1111-1111-1111-111111111111");
        Instant at = Instant.parse("2026-02-01T08:00:00Z");

        Shop shop = Shop.create(id, businessId, ShopName.of("Kilimani"), at);

        assertEquals(id, shop.getId());
        assertEquals(at, shop.getCreatedAt());
        assertEquals(1, shop.pullDomainEvents().size());
    }

    @Test
    void create_rejectsNullBusinessId() {
        assertThrows(InvariantViolationException.class,
                () -> Shop.create(null, ShopName.of("CBD")));
    }

    @Test
    void reconstitute_doesNotRaiseEvents() {
        Shop shop = Shop.reconstitute(
                StableId.generate(),
                businessId,
                ShopName.of("CBD"),
                ShopStatus.ACTIVE,
                Instant.now());

        assertTrue(shop.pullDomainEvents().isEmpty());
    }

    @Test
    void rename_updatesName_whenActive() {
        Shop shop = Shop.create(businessId, ShopName.of("Old"));
        shop.pullDomainEvents();

        shop.rename(ShopName.of("Westlands Premium"));
        assertEquals("Westlands Premium", shop.getName().asString());
    }

    @Test
    void rename_rejectsWhenClosed() {
        Shop shop = Shop.create(businessId, ShopName.of("Westlands"));
        shop.close();

        assertThrows(InvariantViolationException.class,
                () -> shop.rename(ShopName.of("New Name")));
    }

    @Test
    void close_isIdempotent() {
        Shop shop = Shop.create(businessId, ShopName.of("Westlands"));
        shop.close();
        assertEquals(ShopStatus.CLOSED, shop.getStatus());
        assertFalse(shop.isActive());

        shop.close();
        assertEquals(ShopStatus.CLOSED, shop.getStatus());
    }
}
