package com.ratibu.worker.domain;

import com.ratibu.common.domain.StableId;

import java.util.List;
import java.util.Optional;

public interface WorkerRepository {

    Worker save(Worker worker);

    Optional<Worker> findById(StableId id);

    List<Worker> findByBusinessId(StableId businessId);

    Optional<Worker> findByMembershipId(StableId membershipId);

    boolean existsById(StableId id);
}
