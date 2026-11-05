package com.ratibu.account.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;

import java.time.Instant;
import java.util.Objects;

public final class AccountCreated implements DomainEvent {

    private final StableId accountId;
    private final EmailAddress email;
    private final Instant occurredAt;

    public AccountCreated(StableId accountId, EmailAddress email, Instant occurredAt) {
        this.accountId = Objects.requireNonNull(accountId);
        this.email = Objects.requireNonNull(email);
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public StableId accountId() {
        return accountId;
    }

    public EmailAddress email() {
        return email;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
