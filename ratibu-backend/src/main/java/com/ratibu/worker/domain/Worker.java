package com.ratibu.worker.domain;

import com.ratibu.common.domain.AggregateRoot;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Worker Profile — business-level operational record of a person who performs
 * work for the Business (ADR-004 §8–§13, ADR-009, ADR-012 §16, ADR-013 §8.6).
 *
 * <p>Owned here:
 * <ul>
 *   <li>Worker identity ({@link StableId})</li>
 *   <li>Owning Business reference (StableId only)</li>
 *   <li>Display name</li>
 *   <li>Optional link to a Business Membership (same business)</li>
 *   <li>Lifecycle status (ACTIVE / INACTIVE)</li>
 * </ul>
 *
 * <p>Not owned here (separate aggregates / later slices): shop assignments,
 * service capabilities, availability, and User Account identity. Per ADR-013
 * §8.6 the profile attaches to a Membership when a login is present; a worker
 * may exist with no membership (and therefore no User Account) for tracking only.
 */
public final class Worker extends AggregateRoot {

    private final StableId businessId;
    private WorkerName name;
    private StableId membershipId;
    private WorkerStatus status;
    private final Instant createdAt;

    private Worker(
            StableId id,
            StableId businessId,
            WorkerName name,
            StableId membershipId,
            WorkerStatus status,
            Instant createdAt) {
        super(id);
        this.businessId = Objects.requireNonNull(businessId);
        this.name = Objects.requireNonNull(name);
        this.membershipId = membershipId;
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Worker create(StableId businessId, WorkerName name, StableId membershipId) {
        return create(StableId.generate(), businessId, name, membershipId, Instant.now());
    }

    public static Worker create(
            StableId id,
            StableId businessId,
            WorkerName name,
            StableId membershipId,
            Instant createdAt) {
        if (id == null) {
            throw new InvariantViolationException("Worker id must not be null");
        }
        if (businessId == null) {
            throw new InvariantViolationException("Worker businessId must not be null");
        }
        if (name == null) {
            throw new InvariantViolationException("Worker name must not be null");
        }
        Worker worker = new Worker(id, businessId, name, membershipId, WorkerStatus.ACTIVE, createdAt);
        worker.raise(new WorkerCreated(id, businessId, name, membershipId, createdAt));
        return worker;
    }

    public static Worker reconstitute(
            StableId id,
            StableId businessId,
            WorkerName name,
            StableId membershipId,
            WorkerStatus status,
            Instant createdAt) {
        return new Worker(id, businessId, name, membershipId, status, createdAt);
    }

    public StableId getBusinessId() {
        return businessId;
    }

    public WorkerName getName() {
        return name;
    }

    public Optional<StableId> getMembershipId() {
        return Optional.ofNullable(membershipId);
    }

    public WorkerStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return status == WorkerStatus.ACTIVE;
    }

    public void rename(WorkerName newName) {
        if (newName == null) {
            throw new InvariantViolationException("Worker name must not be null");
        }
        if (status == WorkerStatus.INACTIVE) {
            throw new InvariantViolationException("Cannot rename an inactive worker");
        }
        this.name = newName;
    }

    /**
     * Soft-deactivate. Historical bookings and financial records remain (ADR-004 §7 principle).
     */
    public void deactivate() {
        if (status == WorkerStatus.INACTIVE) {
            return; // idempotent
        }
        this.status = WorkerStatus.INACTIVE;
    }

    public void activate() {
        if (status == WorkerStatus.ACTIVE) {
            return;
        }
        this.status = WorkerStatus.ACTIVE;
    }

    /**
     * Link (or re-link) this profile to a Business Membership in the same business.
     * The application layer must verify the membership belongs to {@code businessId}.
     */
    public void linkMembership(StableId membershipId) {
        if (membershipId == null) {
            throw new InvariantViolationException("Membership id must not be null");
        }
        this.membershipId = membershipId;
    }
}
