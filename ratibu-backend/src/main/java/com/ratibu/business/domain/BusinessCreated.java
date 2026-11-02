package com.ratibu.business.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;

import java.time.Instant;
import java.util.Objects;

/**
 * Raised when a Business aggregate is first created. Application services
 * (e.g. membership bootstrap for the creating Owner) react to this event
 * rather than the Business aggregate calling into other aggregates directly
 * (ADR-012 §19).
 */
public final class BusinessCreated implements DomainEvent {

    private final StableId businessId;
    private final BusinessName name;
    private final CurrencyCode currency;
    private final Instant occurredAt;

    public BusinessCreated(StableId businessId, BusinessName name, CurrencyCode currency, Instant occurredAt) {
        this.businessId = Objects.requireNonNull(businessId);
        this.name = Objects.requireNonNull(name);
        this.currency = Objects.requireNonNull(currency);
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public StableId businessId() {
        return businessId;
    }

    public BusinessName name() {
        return name;
    }

    public CurrencyCode currency() {
        return currency;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
