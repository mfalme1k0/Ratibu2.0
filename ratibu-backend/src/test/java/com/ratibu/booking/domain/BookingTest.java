package com.ratibu.booking.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    private final StableId shopId = StableId.fromString("11111111-1111-1111-1111-111111111111");
    private final StableId clientId = StableId.fromString("22222222-2222-2222-2222-222222222222");
    private final StableId serviceId = StableId.fromString("33333333-3333-3333-3333-333333333333");
    private final Money price = Money.of(new BigDecimal("500.00"));
    private final Instant when = Instant.parse("2026-12-01T10:00:00Z");

    @Test
    void create_startsScheduled_andRaisesEvent() {
        Booking b = Booking.create(shopId, clientId, serviceId, null, when, 30, price);

        assertEquals(BookingStatus.SCHEDULED, b.getStatus());
        assertEquals(price, b.getExpectedPrice());
        assertTrue(b.getFinalPrice().isEmpty());
        assertEquals(price, b.amountDue());

        List<DomainEvent> events = b.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(BookingCreated.class, events.getFirst());
    }

    @Test
    void lifecycle_scheduled_to_completed() {
        Booking b = Booking.create(shopId, clientId, serviceId, null, when, null, price);
        b.pullDomainEvents();
        b.start();
        assertEquals(BookingStatus.IN_PROGRESS, b.getStatus());
        b.complete(null);
        assertEquals(BookingStatus.COMPLETED, b.getStatus());
        assertEquals(price, b.getFinalPrice().orElseThrow());
    }

    @Test
    void complete_withFinalPrice() {
        Booking b = Booking.create(shopId, clientId, serviceId, null, when, null, price);
        b.complete(Money.of(new BigDecimal("450.00")));
        assertEquals(Money.of(new BigDecimal("450.00")), b.amountDue());
    }

    @Test
    void cancel_fromScheduled() {
        Booking b = Booking.create(shopId, clientId, serviceId, null, when, null, price);
        b.cancel();
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    @Test
    void noShow_fromScheduled() {
        Booking b = Booking.create(shopId, clientId, serviceId, null, when, null, price);
        b.markNoShow();
        assertEquals(BookingStatus.NO_SHOW, b.getStatus());
    }

    @Test
    void cancel_rejectsInProgress() {
        Booking b = Booking.create(shopId, clientId, serviceId, null, when, null, price);
        b.start();
        assertThrows(InvariantViolationException.class, b::cancel);
    }

    @Test
    void create_rejectsNegativePrice() {
        assertThrows(InvariantViolationException.class,
                () -> Booking.create(shopId, clientId, serviceId, null, when, null,
                        Money.of(new BigDecimal("-1"))));
    }
}
