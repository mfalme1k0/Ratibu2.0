package com.ratibu.membership.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;

import java.time.Instant;
import java.util.Objects;

public final class MembershipCreated implements DomainEvent {

    private final StableId membershipId;
    private final StableId accountId;
    private final StableId businessId;
    private final MembershipRole role;
    private final Instant occurredAt;

    public MembershipCreated(
            StableId membershipId,
            StableId accountId,
            StableId businessId,
            MembershipRole role,
            Instant occurredAt) {
        this.membershipId = Objects.requireNonNull(membershipId);
        this.accountId = Objects.requireNonNull(accountId);
        this.businessId = Objects.requireNonNull(businessId);
        this.role = Objects.requireNonNull(role);
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public StableId membershipId() {
        return membershipId;
    }

    public StableId accountId() {
        return accountId;
    }

    public StableId businessId() {
        return businessId;
    }

    public MembershipRole role() {
        return role;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
