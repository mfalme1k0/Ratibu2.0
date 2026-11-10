package com.ratibu.booking.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateBookingRequest(
        @Size(max = 36, message = "bookingId must be a UUID string")
        String bookingId,

        @NotBlank(message = "shopId is required")
        @Size(max = 36, message = "shopId must be a UUID string")
        String shopId,

        @NotBlank(message = "clientId is required")
        @Size(max = 36, message = "clientId must be a UUID string")
        String clientId,

        @NotBlank(message = "serviceId is required")
        @Size(max = 36, message = "serviceId must be a UUID string")
        String serviceId,

        @Size(max = 36, message = "workerId must be a UUID string")
        String workerId,

        @NotNull(message = "scheduledAt is required")
        Instant scheduledAt,

        @Positive(message = "durationMinutes must be positive")
        Integer durationMinutes,

        @NotNull(message = "expectedPrice is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "expectedPrice must not be negative")
        BigDecimal expectedPrice
) {
}
