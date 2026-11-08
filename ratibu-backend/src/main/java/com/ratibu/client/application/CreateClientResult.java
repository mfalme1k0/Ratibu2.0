package com.ratibu.client.application;

import java.time.Instant;

public record CreateClientResult(
        String clientId,
        String businessId,
        String name,
        String phone,
        String email,
        String notes,
        String status,
        Instant createdAt,
        boolean alreadyExisted
) {
}
