package com.ratibu.worker.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkerRequest(
        @Size(max = 36, message = "workerId must be a UUID string")
        String workerId,

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @Size(max = 36, message = "membershipId must be a UUID string")
        String membershipId
) {
}
