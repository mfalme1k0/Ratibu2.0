package com.ratibu.client.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @Size(max = 36, message = "clientId must be a UUID string")
        String clientId,

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @Size(max = 32, message = "phone must be at most 32 characters")
        String phone,

        @Size(max = 254, message = "email must be at most 254 characters")
        String email,

        @Size(max = 2000, message = "notes must be at most 2000 characters")
        String notes
) {
}
