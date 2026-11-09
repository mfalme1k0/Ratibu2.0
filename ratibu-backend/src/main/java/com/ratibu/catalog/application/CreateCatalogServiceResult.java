package com.ratibu.catalog.application;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateCatalogServiceResult(
        String serviceId,
        String businessId,
        String name,
        String description,
        BigDecimal defaultPrice,
        String status,
        Instant createdAt,
        boolean alreadyExisted
) {
}
