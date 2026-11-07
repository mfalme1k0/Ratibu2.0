package com.ratibu.worker.api;

import com.ratibu.worker.application.CreateWorkerResult;

import java.time.Instant;

public record WorkerResponse(
        String workerId,
        String businessId,
        String name,
        String membershipId,
        String status,
        Instant createdAt,
        boolean alreadyExisted
) {
    public static WorkerResponse from(CreateWorkerResult result) {
        return new WorkerResponse(
                result.workerId(),
                result.businessId(),
                result.name(),
                result.membershipId(),
                result.status(),
                result.createdAt(),
                result.alreadyExisted());
    }
}
