package com.ratibu.catalog.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;

import java.time.Instant;
import java.util.Objects;

public final class CatalogServiceCreated implements DomainEvent {

    private final StableId serviceId;
    private final StableId businessId;
    private final ServiceName name;
    private final Money defaultPrice;
    private final Instant occurredAt;

    public CatalogServiceCreated(
            StableId serviceId,
            StableId businessId,
            ServiceName name,
            Money defaultPrice,
            Instant occurredAt) {
        this.serviceId = Objects.requireNonNull(serviceId);
        this.businessId = Objects.requireNonNull(businessId);
        this.name = Objects.requireNonNull(name);
        this.defaultPrice = Objects.requireNonNull(defaultPrice);
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public StableId serviceId() {
        return serviceId;
    }

    public StableId businessId() {
        return businessId;
    }

    public ServiceName name() {
        return name;
    }

    public Money defaultPrice() {
        return defaultPrice;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
