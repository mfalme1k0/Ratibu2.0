package com.ratibu.client.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class ClientCreated implements DomainEvent {

    private final StableId clientId;
    private final StableId businessId;
    private final ClientName name;
    private final PhoneNumber phone;
    private final Instant occurredAt;

    public ClientCreated(
            StableId clientId,
            StableId businessId,
            ClientName name,
            PhoneNumber phone,
            Instant occurredAt) {
        this.clientId = Objects.requireNonNull(clientId);
        this.businessId = Objects.requireNonNull(businessId);
        this.name = Objects.requireNonNull(name);
        this.phone = phone;
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public StableId clientId() {
        return clientId;
    }

    public StableId businessId() {
        return businessId;
    }

    public ClientName name() {
        return name;
    }

    public Optional<PhoneNumber> phone() {
        return Optional.ofNullable(phone);
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
