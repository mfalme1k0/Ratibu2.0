package com.ratibu.business.domain;

import com.ratibu.common.domain.AggregateRoot;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root for the organizational boundary of a Ratibu deployment
 * (ADR-003, ADR-012 §3.1 / §16).
 *
 * <p>Responsibilities owned here:
 * <ul>
 *   <li>Business identity ({@link StableId})</li>
 *   <li>Display name</li>
 *   <li>Single business currency (ADR-011 §49)</li>
 * </ul>
 *
 * <p>Not owned here (separate aggregates linked by StableId):
 * memberships/ownership, shops, clients, services, bookings, payments,
 * cash book, insights. The Business does not embed those collections so a
 * multi-shop business does not become one enormous aggregate (ADR-012 §3.1).
 *
 * <p>A Business must eventually have at least one Shop for normal operation
 * (ADR-003 §6); that invariant is enforced when the first Shop is created
 * / when operational use cases run, not at Business construction time, so
 * that create-business and create-first-shop can be separate use cases
 * (and separate offline operations).
 */
public final class Business extends AggregateRoot {

    private BusinessName name;
    private final CurrencyCode currency;
    private final Instant createdAt;

    private Business(StableId id, BusinessName name, CurrencyCode currency, Instant createdAt) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.currency = Objects.requireNonNull(currency);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    /**
     * Factory for a new Business. Generates a client-safe {@link StableId}
     * so the same method can be used offline (ADR-010 §14). Raises
     * {@link BusinessCreated}.
     */
    public static Business create(BusinessName name, CurrencyCode currency) {
        return create(StableId.generate(), name, currency, Instant.now());
    }

    /**
     * Reconstruction / offline-sync factory: caller supplies the StableId
     * and timestamp (e.g. from a client payload or from persistence).
     */
    public static Business create(StableId id, BusinessName name, CurrencyCode currency, Instant createdAt) {
        if (id == null) {
            throw new InvariantViolationException("Business id must not be null");
        }
        Business business = new Business(id, name, currency, createdAt);
        business.raise(new BusinessCreated(id, name, currency, createdAt));
        return business;
    }

    /**
     * Reconstitute an existing Business from persistence without raising
     * domain events. Used by infrastructure mappers only.
     */
    public static Business reconstitute(StableId id, BusinessName name, CurrencyCode currency, Instant createdAt) {
        return new Business(id, name, currency, createdAt);
    }

    public BusinessName getName() {
        return name;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Rename the business. Currency is intentionally immutable after creation
     * in MVP (changing currency would require a financial migration ADR).
     */
    public void rename(BusinessName newName) {
        if (newName == null) {
            throw new InvariantViolationException("Business name must not be null");
        }
        this.name = newName;
    }
}
