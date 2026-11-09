package com.ratibu.catalog.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCatalogServiceRequest(
        @Size(max = 36, message = "serviceId must be a UUID string")
        String serviceId,

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description,

        @NotNull(message = "defaultPrice is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "defaultPrice must not be negative")
        BigDecimal defaultPrice
) {
}
