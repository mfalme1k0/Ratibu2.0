package com.ratibu.shop.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;

import java.time.Instant;
import java.util.Objects;

/**
 * Raised when a Shop is first created. Application services may react
 * (e.g. bootstrap default catalog bindings) without the Shop aggregate
 * calling other aggregates directly (ADR-012 §19).
 */
public final class ShopCreated implements DomainEvent {

    private final StableId shopId;
    private final StableId businessId;
    private final ShopName name;
    private final Instant occurredAt;

    public ShopCreated(StableId shopId, StableId businessId, ShopName name, Instant occurredAt) {
        this.shopId = Objects.requireNonNull(shopId);
        this.businessId = Objects.requireNonNull(businessId);
        this.name = Objects.requireNonNull(name);
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public StableId shopId() {
        return shopId;
    }

    public StableId businessId() {
        return businessId;
    }

    public ShopName name() {
        return name;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
