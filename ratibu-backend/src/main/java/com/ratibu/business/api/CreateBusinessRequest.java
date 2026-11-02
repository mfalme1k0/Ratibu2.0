package com.ratibu.business.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * HTTP body for {@code POST /api/v1/businesses}.
 *
 * <p>{@code businessId} is optional: clients generating StableIds offline
 * (ADR-010) may supply it for idempotent create.
 */
public record CreateBusinessRequest(
        @Size(max = 36, message = "businessId must be a UUID string")
        String businessId,

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @NotBlank(message = "currency is required")
        @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO 4217 code")
        String currency
) {
}
