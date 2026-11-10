package com.ratibu.booking.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public record CreateBookingCommand(
        Optional<String> bookingId,
        String shopId,
        String clientId,
        String serviceId,
        Optional<String> workerId,
        Instant scheduledAt,
        Optional<Integer> durationMinutes,
        BigDecimal expectedPrice
) {
    public CreateBookingCommand {
        bookingId = bookingId == null ? Optional.empty() : bookingId;
        workerId = workerId == null ? Optional.empty() : workerId;
        durationMinutes = durationMinutes == null ? Optional.empty() : durationMinutes;
    }
}
