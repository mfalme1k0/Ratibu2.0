package com.ratibu.membership.domain;

import com.ratibu.common.domain.AggregateRoot;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;

/**
 * Business Membership — links a User Account to a Business with a role
 * (ADR-002 §5–6). Shop assignments are deferred to a later slice.
 *
 * <p>Account and business ids are fixed at creation. Role may change via
 * {@link #changeRole}.
 */
public final class Membership extends AggregateRoot {

    private final StableId accountId;
    private final StableId businessId;
    private MembershipRole role;
    private final Instant createdAt;

    private Membership(
            StableId id,
            StableId accountId,
            StableId businessId,
            MembershipRole role,
            Instant createdAt) {
        super(id);
        this.accountId = Objects.requireNonNull(accountId);
        this.businessId = Objects.requireNonNull(businessId);
        this.role = Objects.requireNonNull(role);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Membership create(StableId accountId, StableId businessId, MembershipRole role) {
        return create(StableId.generate(), accountId, businessId, role, Instant.now());
    }

    public static Membership create(
            StableId id,
            StableId accountId,
            StableId businessId,
            MembershipRole role,
            Instant createdAt) {
        if (id == null) {
            throw new InvariantViolationException("Membership id must not be null");
        }
        if (accountId == null) {
            throw new InvariantViolationException("Membership accountId must not be null");
        }
        if (businessId == null) {
            throw new InvariantViolationException("Membership businessId must not be null");
        }
        if (role == null) {
            throw new InvariantViolationException("Membership role must not be null");
        }
        Membership membership = new Membership(id, accountId, businessId, role, createdAt);
        membership.raise(new MembershipCreated(id, accountId, businessId, role, createdAt));
        return membership;
    }

    public static Membership reconstitute(
            StableId id,
            StableId accountId,
            StableId businessId,
            MembershipRole role,
            Instant createdAt) {
        return new Membership(id, accountId, businessId, role, createdAt);
    }

    public StableId getAccountId() {
        return accountId;
    }

    public StableId getBusinessId() {
        return businessId;
    }

    public MembershipRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void changeRole(MembershipRole newRole) {
        if (newRole == null) {
            throw new InvariantViolationException("Membership role must not be null");
        }
        this.role = newRole;
    }
}
