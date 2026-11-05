package com.ratibu.membership.api;

import com.ratibu.membership.application.CreateMembershipResult;

import java.time.Instant;

public record MembershipResponse(
        String membershipId,
        String accountId,
        String businessId,
        String role,
        Instant createdAt,
        boolean alreadyExisted
) {
    public static MembershipResponse from(CreateMembershipResult result) {
        return new MembershipResponse(
                result.membershipId(),
                result.accountId(),
                result.businessId(),
                result.role(),
                result.createdAt(),
                result.alreadyExisted());
    }
}
