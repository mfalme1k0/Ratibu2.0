package com.ratibu.worker.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class WorkerCreated implements DomainEvent {

    private final StableId workerId;
    private final StableId businessId;
    private final WorkerName name;
    private final StableId membershipId;
    private final Instant occurredAt;

    public WorkerCreated(
            StableId workerId,
            StableId businessId,
            WorkerName name,
            StableId membershipId,
            Instant occurredAt) {
        this.workerId = Objects.requireNonNull(workerId);
        this.businessId = Objects.requireNonNull(businessId);
        this.name = Objects.requireNonNull(name);
        this.membershipId = membershipId; // optional
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public StableId workerId() {
        return workerId;
    }

    public StableId businessId() {
        return businessId;
    }

    public WorkerName name() {
        return name;
    }

    public Optional<StableId> membershipId() {
        return Optional.ofNullable(membershipId);
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
