package com.ratibu.booking.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;

import java.time.Instant;
import java.util.Objects;

public final class BookingCreated implements DomainEvent {

    private final StableId bookingId;
    private final StableId shopId;
    private final StableId clientId;
    private final StableId serviceId;
    private final Money expectedPrice;
    private final Instant scheduledAt;
    private final Instant occurredAt;

    public BookingCreated(
            StableId bookingId,
            StableId shopId,
            StableId clientId,
            StableId serviceId,
            Money expectedPrice,
            Instant scheduledAt,
            Instant occurredAt) {
        this.bookingId = Objects.requireNonNull(bookingId);
        this.shopId = Objects.requireNonNull(shopId);
        this.clientId = Objects.requireNonNull(clientId);
        this.serviceId = Objects.requireNonNull(serviceId);
        this.expectedPrice = Objects.requireNonNull(expectedPrice);
        this.scheduledAt = Objects.requireNonNull(scheduledAt);
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public StableId bookingId() {
        return bookingId;
    }

    public StableId shopId() {
        return shopId;
    }

    public StableId clientId() {
        return clientId;
    }

    public StableId serviceId() {
        return serviceId;
    }

    public Money expectedPrice() {
        return expectedPrice;
    }

    public Instant scheduledAt() {
        return scheduledAt;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
