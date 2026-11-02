package com.ratibu.business.api;

import com.ratibu.business.application.CreateBusinessResult;

import java.time.Instant;

/**
 * HTTP representation of a Business after create (and later for GET).
 */
public record BusinessResponse(
        String businessId,
        String name,
        String currency,
        Instant createdAt,
        boolean alreadyExisted
) {
    public static BusinessResponse from(CreateBusinessResult result) {
        return new BusinessResponse(
                result.businessId(),
                result.name(),
                result.currency(),
                result.createdAt(),
                result.alreadyExisted());
    }
}
