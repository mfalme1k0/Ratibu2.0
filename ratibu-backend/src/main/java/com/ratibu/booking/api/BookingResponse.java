package com.ratibu.booking.api;

import com.ratibu.booking.application.CreateBookingResult;

import java.math.BigDecimal;
import java.time.Instant;

public record BookingResponse(
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
    public static BookingResponse from(CreateBookingResult result) {
        return new BookingResponse(
                result.bookingId(),
                result.shopId(),
                result.clientId(),
                result.serviceId(),
                result.workerId(),
                result.scheduledAt(),
                result.durationMinutes(),
                result.expectedPrice(),
                result.finalPrice(),
                result.status(),
                result.createdAt(),
                result.alreadyExisted());
    }
}
