package com.ratibu.shop.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body for {@code POST /api/v1/businesses/{businessId}/shops}.
 */
public record CreateShopRequest(
        @Size(max = 36, message = "shopId must be a UUID string")
        String shopId,

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name
) {
}
