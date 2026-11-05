package com.ratibu.membership.application;

import java.util.Optional;

public record CreateMembershipCommand(
        Optional<String> membershipId,
        String accountId,
        String businessId,
        String role
) {
    public CreateMembershipCommand {
        membershipId = membershipId == null ? Optional.empty() : membershipId;
    }

    public static CreateMembershipCommand of(String accountId, String businessId, String role) {
        return new CreateMembershipCommand(Optional.empty(), accountId, businessId, role);
    }
}
