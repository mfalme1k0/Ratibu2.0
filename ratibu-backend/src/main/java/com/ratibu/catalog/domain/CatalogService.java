package com.ratibu.catalog.domain;

import com.ratibu.common.domain.AggregateRoot;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Business-level service definition (ADR-004 §14–§19, ADR-012 §16).
 * Packaged under {@code catalog} to avoid clashing with Spring's {@code @Service}.
 *
 * <p>Owned here:
 * <ul>
 *   <li>Service identity ({@link StableId})</li>
 *   <li>Owning Business reference</li>
 *   <li>Display name and optional description</li>
 *   <li>Default price (currency is on Business — {@link Money} has no currency code)</li>
 *   <li>Lifecycle status (ACTIVE / DISCONTINUED)</li>
 * </ul>
 *
 * <p>Not owned here (later slices): shop-specific availability/pricing (ADR-004 §15–§16),
 * worker–service capabilities (ADR-004 §13), historical price snapshots on bookings.
 */
public final class CatalogService extends AggregateRoot {

    private final StableId businessId;
    private ServiceName name;
    private String description;
    private Money defaultPrice;
    private ServiceStatus status;
    private final Instant createdAt;

    private CatalogService(
            StableId id,
            StableId businessId,
            ServiceName name,
            String description,
            Money defaultPrice,
            ServiceStatus status,
            Instant createdAt) {
        super(id);
        this.businessId = Objects.requireNonNull(businessId);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.defaultPrice = Objects.requireNonNull(defaultPrice);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static CatalogService create(
            StableId businessId, ServiceName name, String description, Money defaultPrice) {
        return create(StableId.generate(), businessId, name, description, defaultPrice, Instant.now());
    }

    public static CatalogService create(
            StableId id,
            StableId businessId,
            ServiceName name,
            String description,
            Money defaultPrice,
            Instant createdAt) {
        if (id == null) {
            throw new InvariantViolationException("Service id must not be null");
        }
        if (businessId == null) {
            throw new InvariantViolationException("Service businessId must not be null");
        }
        if (name == null) {
            throw new InvariantViolationException("Service name must not be null");
        }
        if (defaultPrice == null) {
            throw new InvariantViolationException("Service defaultPrice must not be null");
        }
        if (defaultPrice.isNegative()) {
            throw new InvariantViolationException("Service defaultPrice must not be negative");
        }
        String normalizedDescription = normalizeDescription(description);
        CatalogService service = new CatalogService(
                id, businessId, name, normalizedDescription, defaultPrice,
                ServiceStatus.ACTIVE, createdAt);
        service.raise(new CatalogServiceCreated(id, businessId, name, defaultPrice, createdAt));
        return service;
    }

    public static CatalogService reconstitute(
            StableId id,
            StableId businessId,
            ServiceName name,
            String description,
            Money defaultPrice,
            ServiceStatus status,
            Instant createdAt) {
        return new CatalogService(id, businessId, name, description, defaultPrice, status, createdAt);
    }

    private static String normalizeDescription(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 2000) {
            throw new InvariantViolationException("Service description must be at most 2000 characters");
        }
        return trimmed;
    }

    public StableId getBusinessId() {
        return businessId;
    }

    public ServiceName getName() {
        return name;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Money getDefaultPrice() {
        return defaultPrice;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return status == ServiceStatus.ACTIVE;
    }

    public void rename(ServiceName newName) {
        requireActive("rename");
        if (newName == null) {
            throw new InvariantViolationException("Service name must not be null");
        }
        this.name = newName;
    }

    public void changeDescription(String newDescription) {
        requireActive("change description");
        this.description = normalizeDescription(newDescription);
    }

    /**
     * Updates the current default price. Does not rewrite historical bookings
     * (ADR-004 §16–§17) — those capture price at booking time elsewhere.
     */
    public void changeDefaultPrice(Money newPrice) {
        requireActive("change default price");
        if (newPrice == null) {
            throw new InvariantViolationException("Service defaultPrice must not be null");
        }
        if (newPrice.isNegative()) {
            throw new InvariantViolationException("Service defaultPrice must not be negative");
        }
        this.defaultPrice = newPrice;
    }

    public void discontinue() {
        if (status == ServiceStatus.DISCONTINUED) {
            return;
        }
        this.status = ServiceStatus.DISCONTINUED;
    }

    public void reactivate() {
        if (status == ServiceStatus.ACTIVE) {
            return;
        }
        this.status = ServiceStatus.ACTIVE;
    }

    private void requireActive(String action) {
        if (status == ServiceStatus.DISCONTINUED) {
            throw new InvariantViolationException("Cannot " + action + " a discontinued service");
        }
    }
}
