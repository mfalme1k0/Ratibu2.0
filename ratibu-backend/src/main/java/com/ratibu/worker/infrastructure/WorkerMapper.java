package com.ratibu.worker.infrastructure;

import com.ratibu.common.domain.StableId;
import com.ratibu.worker.domain.Worker;
import com.ratibu.worker.domain.WorkerName;
import com.ratibu.worker.domain.WorkerStatus;

final class WorkerMapper {

    private WorkerMapper() {
    }

    static WorkerJpaEntity toEntity(Worker worker) {
        return new WorkerJpaEntity(
                worker.getId().asUuid(),
                worker.getBusinessId().asUuid(),
                worker.getName().asString(),
                worker.getMembershipId().map(StableId::asUuid).orElse(null),
                worker.getStatus().name(),
                worker.getCreatedAt());
    }

    static void updateEntity(WorkerJpaEntity entity, Worker worker) {
        entity.setName(worker.getName().asString());
        entity.setMembershipId(worker.getMembershipId().map(StableId::asUuid).orElse(null));
        entity.setStatus(worker.getStatus().name());
    }

    static Worker toDomain(WorkerJpaEntity entity) {
        return Worker.reconstitute(
                StableId.fromUuid(entity.getId()),
                StableId.fromUuid(entity.getBusinessId()),
                WorkerName.of(entity.getName()),
                entity.getMembershipId() != null ? StableId.fromUuid(entity.getMembershipId()) : null,
                WorkerStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt());
    }
}
