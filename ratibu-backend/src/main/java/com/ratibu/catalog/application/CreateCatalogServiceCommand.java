package com.ratibu.catalog.application;

import java.math.BigDecimal;
import java.util.Optional;

public record CreateCatalogServiceCommand(
        Optional<String> serviceId,
        String businessId,
        String name,
        Optional<String> description,
        BigDecimal defaultPrice
) {
    public CreateCatalogServiceCommand {
        serviceId = serviceId == null ? Optional.empty() : serviceId;
        description = description == null ? Optional.empty() : description;
    }

    public static CreateCatalogServiceCommand of(String businessId, String name, BigDecimal defaultPrice) {
        return new CreateCatalogServiceCommand(
                Optional.empty(), businessId, name, Optional.empty(), defaultPrice);
    }
}
