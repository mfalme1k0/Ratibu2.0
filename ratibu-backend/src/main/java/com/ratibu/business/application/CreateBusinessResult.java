package com.ratibu.business.application;

import java.time.Instant;

/**
 * Outcome of {@link CreateBusinessService#execute}. Carries only what the
 * caller needs for the API response or for further use-case chaining
 * (e.g. bootstrap Owner membership from {@link com.ratibu.business.domain.BusinessCreated}).
 */
public record CreateBusinessResult(
        String businessId,
        String name,
        String currency,
        Instant createdAt,
        boolean alreadyExisted
) {
}
