package com.ratibu.client.api;

import com.ratibu.client.application.CreateClientResult;

import java.time.Instant;

public record ClientResponse(
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
    public static ClientResponse from(CreateClientResult result) {
        return new ClientResponse(
                result.clientId(),
                result.businessId(),
                result.name(),
                result.phone(),
                result.email(),
                result.notes(),
                result.status(),
                result.createdAt(),
                result.alreadyExisted());
    }
}
