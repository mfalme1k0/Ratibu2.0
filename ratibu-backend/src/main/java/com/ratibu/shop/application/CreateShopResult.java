package com.ratibu.shop.application;

import java.time.Instant;

public record CreateShopResult(
        String shopId,
        String businessId,
        String name,
        String status,
        Instant createdAt,
        boolean alreadyExisted
) {
}
