package com.ratibu.shop.api;

import com.ratibu.shop.application.CreateShopResult;

import java.time.Instant;

public record ShopResponse(
        String shopId,
        String businessId,
        String name,
        String status,
        Instant createdAt,
        boolean alreadyExisted
) {
    public static ShopResponse from(CreateShopResult result) {
        return new ShopResponse(
                result.shopId(),
                result.businessId(),
                result.name(),
                result.status(),
                result.createdAt(),
                result.alreadyExisted());
    }
}
