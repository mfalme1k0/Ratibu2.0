package com.ratibu.worker.infrastructure;

import com.ratibu.common.domain.StableId;
import com.ratibu.worker.domain.Worker;
import com.ratibu.worker.domain.WorkerRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class WorkerRepositoryAdapter implements WorkerRepository {

    private final SpringDataWorkerRepository springData;

    public WorkerRepositoryAdapter(SpringDataWorkerRepository springData) {
        this.springData = springData;
    }

    @Override
    public Worker save(Worker worker) {
        Optional<WorkerJpaEntity> existing = springData.findById(worker.getId().asUuid());
        WorkerJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            WorkerMapper.updateEntity(entity, worker);
        } else {
            entity = WorkerMapper.toEntity(worker);
        }
        return WorkerMapper.toDomain(springData.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Worker> findById(StableId id) {
        return springData.findById(id.asUuid()).map(WorkerMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> findByBusinessId(StableId businessId) {
        return springData.findByBusinessId(businessId.asUuid()).stream()
                .map(WorkerMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Worker> findByMembershipId(StableId membershipId) {
        return springData.findByMembershipId(membershipId.asUuid()).map(WorkerMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(StableId id) {
        return springData.existsById(id.asUuid());
    }
}
