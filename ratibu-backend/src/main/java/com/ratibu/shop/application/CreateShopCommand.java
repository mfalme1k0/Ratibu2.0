package com.ratibu.shop.application;

import java.util.Optional;

/**
 * Input for create-shop. {@code shopId} is optional for offline StableId sync (ADR-010).
 * {@code businessId} is required (ADR-003: every Shop belongs to one Business).
 */
public record CreateShopCommand(
        Optional<String> shopId,
        String businessId,
        String name
) {
    public CreateShopCommand {
        shopId = shopId == null ? Optional.empty() : shopId;
    }

    public static CreateShopCommand of(String businessId, String name) {
        return new CreateShopCommand(Optional.empty(), businessId, name);
    }

    public static CreateShopCommand of(String shopId, String businessId, String name) {
        return new CreateShopCommand(Optional.ofNullable(shopId), businessId, name);
    }
}
