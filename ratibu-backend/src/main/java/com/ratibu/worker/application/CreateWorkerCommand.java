package com.ratibu.worker.application;

import java.util.Optional;

public record CreateWorkerCommand(
        Optional<String> workerId,
        String businessId,
        String name,
        Optional<String> membershipId
) {
    public CreateWorkerCommand {
        workerId = workerId == null ? Optional.empty() : workerId;
        membershipId = membershipId == null ? Optional.empty() : membershipId;
    }

    public static CreateWorkerCommand of(String businessId, String name) {
        return new CreateWorkerCommand(Optional.empty(), businessId, name, Optional.empty());
    }

    public static CreateWorkerCommand of(String businessId, String name, String membershipId) {
        return new CreateWorkerCommand(Optional.empty(), businessId, name, Optional.ofNullable(membershipId));
    }
}
