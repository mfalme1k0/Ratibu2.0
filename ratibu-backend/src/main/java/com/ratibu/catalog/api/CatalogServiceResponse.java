package com.ratibu.catalog.api;

import com.ratibu.catalog.application.CreateCatalogServiceResult;

import java.math.BigDecimal;
import java.time.Instant;

public record CatalogServiceResponse(
        String serviceId,
        String businessId,
        String name,
        String description,
        BigDecimal defaultPrice,
        String status,
        Instant createdAt,
        boolean alreadyExisted
) {
    public static CatalogServiceResponse from(CreateCatalogServiceResult result) {
        return new CatalogServiceResponse(
                result.serviceId(),
                result.businessId(),
                result.name(),
                result.description(),
                result.defaultPrice(),
                result.status(),
                result.createdAt(),
                result.alreadyExisted());
    }
}
