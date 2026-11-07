package com.ratibu.worker.application;

import com.ratibu.common.domain.StableId;
import com.ratibu.worker.domain.Worker;
import com.ratibu.worker.domain.WorkerRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryWorkerRepository implements WorkerRepository {

    private final Map<StableId, Worker> store = new ConcurrentHashMap<>();

    @Override
    public Worker save(Worker worker) {
        Worker snap = Worker.reconstitute(
                worker.getId(),
                worker.getBusinessId(),
                worker.getName(),
                worker.getMembershipId().orElse(null),
                worker.getStatus(),
                worker.getCreatedAt());
        store.put(snap.getId(), snap);
        return snap;
    }

    @Override
    public Optional<Worker> findById(StableId id) {
        return Optional.ofNullable(store.get(id)).map(this::copy);
    }

    @Override
    public List<Worker> findByBusinessId(StableId businessId) {
        return store.values().stream()
                .filter(w -> w.getBusinessId().equals(businessId))
                .map(this::copy)
                .toList();
    }

    @Override
    public Optional<Worker> findByMembershipId(StableId membershipId) {
        return store.values().stream()
                .filter(w -> w.getMembershipId().isPresent() && w.getMembershipId().get().equals(membershipId))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public boolean existsById(StableId id) {
        return store.containsKey(id);
    }

    private Worker copy(Worker w) {
        return Worker.reconstitute(
                w.getId(),
                w.getBusinessId(),
                w.getName(),
                w.getMembershipId().orElse(null),
                w.getStatus(),
                w.getCreatedAt());
    }
}
