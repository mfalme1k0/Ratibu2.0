package com.ratibu.account.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @Size(max = 36, message = "accountId must be a UUID string")
        String accountId,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 254, message = "email must be at most 254 characters")
        String email,

        @Size(max = 120, message = "displayName must be at most 120 characters")
        String displayName
) {
}
