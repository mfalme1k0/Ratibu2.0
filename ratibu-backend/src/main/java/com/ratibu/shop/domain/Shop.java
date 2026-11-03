package com.ratibu.shop.domain;

import com.ratibu.common.domain.AggregateRoot;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root for the operational and financial boundary within a Business
 * (ADR-003 §5–§15, ADR-012 §4 / §16).
 *
 * <p>Owned here:
 * <ul>
 *   <li>Shop identity ({@link StableId})</li>
 *   <li>Owning Business reference (StableId only — not a Business object)</li>
 *   <li>Display name</li>
 *   <li>Lifecycle status (ACTIVE / CLOSED)</li>
 * </ul>
 *
 * <p>Not owned here (separate aggregates linked by StableId): bookings,
 * payments, expenses, cash book, services, worker assignments.
 *
 * <p>Every Shop belongs to exactly one Business (ADR-003 Rule 3). The
 * businessId is fixed at creation and is not changeable.
 */
public final class Shop extends AggregateRoot {

    private final StableId businessId;
    private ShopName name;
    private ShopStatus status;
    private final Instant createdAt;

    private Shop(
            StableId id,
            StableId businessId,
            ShopName name,
            ShopStatus status,
            Instant createdAt) {
        super(id);
        this.businessId = Objects.requireNonNull(businessId);
        this.name = Objects.requireNonNull(name);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    /**
     * Factory for a new ACTIVE shop. Generates a client-safe StableId
     * (ADR-010 §14). Raises {@link ShopCreated}.
     */
    public static Shop create(StableId businessId, ShopName name) {
        return create(StableId.generate(), businessId, name, Instant.now());
    }

    /**
     * Offline-sync / explicit-id factory.
     */
    public static Shop create(StableId id, StableId businessId, ShopName name, Instant createdAt) {
        if (id == null) {
            throw new InvariantViolationException("Shop id must not be null");
        }
        if (businessId == null) {
            throw new InvariantViolationException("Shop businessId must not be null");
        }
        Shop shop = new Shop(id, businessId, name, ShopStatus.ACTIVE, createdAt);
        shop.raise(new ShopCreated(id, businessId, name, createdAt));
        return shop;
    }

    /**
     * Reconstitute from persistence without raising domain events.
     */
    public static Shop reconstitute(
            StableId id,
            StableId businessId,
            ShopName name,
            ShopStatus status,
            Instant createdAt) {
        return new Shop(id, businessId, name, status, createdAt);
    }

    public StableId getBusinessId() {
        return businessId;
    }

    public ShopName getName() {
        return name;
    }

    public ShopStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return status == ShopStatus.ACTIVE;
    }

    public void rename(ShopName newName) {
        if (newName == null) {
            throw new InvariantViolationException("Shop name must not be null");
        }
        if (status == ShopStatus.CLOSED) {
            throw new InvariantViolationException("Cannot rename a closed shop");
        }
        this.name = newName;
    }

    /**
     * Close the shop. Historical records remain; identity is unchanged (ADR-003 §14).
     */
    public void close() {
        if (status == ShopStatus.CLOSED) {
            return; // idempotent
        }
        this.status = ShopStatus.CLOSED;
    }
}
