package com.ratibu.worker.application;

import java.time.Instant;

public record CreateWorkerResult(
        String workerId,
        String businessId,
        String name,
        String membershipId,
        String status,
        Instant createdAt,
        boolean alreadyExisted
) {
}
