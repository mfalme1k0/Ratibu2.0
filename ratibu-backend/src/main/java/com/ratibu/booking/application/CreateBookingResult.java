package com.ratibu.booking.application;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateBookingResult(
        String bookingId,
        String shopId,
        String clientId,
        String serviceId,
        String workerId,
        Instant scheduledAt,
        Integer durationMinutes,
        BigDecimal expectedPrice,
        BigDecimal finalPrice,
        String status,
        Instant createdAt,
        boolean alreadyExisted
) {
}
