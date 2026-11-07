package com.ratibu.worker.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataWorkerRepository extends JpaRepository<WorkerJpaEntity, UUID> {

    List<WorkerJpaEntity> findByBusinessId(UUID businessId);

    Optional<WorkerJpaEntity> findByMembershipId(UUID membershipId);
}
