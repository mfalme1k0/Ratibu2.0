package com.ratibu.membership.application;

import java.time.Instant;

public record CreateMembershipResult(
        String membershipId,
        String accountId,
        String businessId,
        String role,
        Instant createdAt,
        boolean alreadyExisted
) {
}
