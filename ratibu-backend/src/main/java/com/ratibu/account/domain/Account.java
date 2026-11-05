package com.ratibu.account.domain;

import com.ratibu.common.domain.AggregateRoot;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * User Account — global authentication identity, independent of any Business
 * (ADR-002 §4). Roles live on Business Membership, not here.
 */
public final class Account extends AggregateRoot {

    private final EmailAddress email;
    private DisplayName displayName;
    private final Instant createdAt;

    private Account(StableId id, EmailAddress email, DisplayName displayName, Instant createdAt) {
        super(id);
        this.email = Objects.requireNonNull(email);
        this.displayName = displayName;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Account create(EmailAddress email, DisplayName displayName) {
        return create(StableId.generate(), email, displayName, Instant.now());
    }

    public static Account create(StableId id, EmailAddress email, DisplayName displayName, Instant createdAt) {
        if (id == null) {
            throw new InvariantViolationException("Account id must not be null");
        }
        Account account = new Account(id, email, displayName, createdAt);
        account.raise(new AccountCreated(id, email, createdAt));
        return account;
    }

    public static Account reconstitute(StableId id, EmailAddress email, DisplayName displayName, Instant createdAt) {
        return new Account(id, email, displayName, createdAt);
    }

    public EmailAddress getEmail() {
        return email;
    }

    public Optional<DisplayName> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Email is immutable after create in MVP (changing login identity needs a
     * dedicated flow and uniqueness checks).
     */
    public void rename(DisplayName newName) {
        if (newName == null) {
            throw new InvariantViolationException("Display name must not be null");
        }
        this.displayName = newName;
    }
}
