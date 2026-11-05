package com.ratibu.membership.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMembershipRequest(
        @Size(max = 36, message = "membershipId must be a UUID string")
        String membershipId,

        @NotBlank(message = "accountId is required")
        @Size(max = 36, message = "accountId must be a UUID string")
        String accountId,

        @NotBlank(message = "role is required")
        @Size(max = 16, message = "role must be at most 16 characters")
        String role
) {
}
