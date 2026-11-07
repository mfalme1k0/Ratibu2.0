package com.ratibu.worker.application;

import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.exception.NotFoundException;
import com.ratibu.membership.domain.Membership;
import com.ratibu.membership.domain.MembershipRepository;
import com.ratibu.worker.domain.Worker;
import com.ratibu.worker.domain.WorkerName;
import com.ratibu.worker.domain.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Creates a Worker Profile under a Business (ADR-004 §8, ADR-013 §8.6).
 * Coordinates via repository ports only. Shop assignments and service
 * capabilities are deferred to later slices.
 */
@Service
public class CreateWorkerService {

    private final WorkerRepository workerRepository;
    private final BusinessRepository businessRepository;
    private final MembershipRepository membershipRepository;

    public CreateWorkerService(
            WorkerRepository workerRepository,
            BusinessRepository businessRepository,
            MembershipRepository membershipRepository) {
        this.workerRepository = workerRepository;
        this.businessRepository = businessRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public CreateWorkerResult execute(CreateWorkerCommand command) {
        StableId businessId = StableId.fromString(command.businessId());
        WorkerName name = WorkerName.of(command.name());

        if (!businessRepository.existsById(businessId)) {
            throw new NotFoundException("Business not found: " + command.businessId());
        }

        StableId membershipId = null;
        if (command.membershipId().isPresent()) {
            String raw = command.membershipId().get();
            if (raw == null || raw.isBlank()) {
                throw new InvariantViolationException("membershipId must not be blank when provided");
            }
            membershipId = StableId.fromString(raw);
            Optional<Membership> membership = membershipRepository.findById(membershipId);
            if (membership.isEmpty()) {
                throw new NotFoundException("Membership not found: " + raw);
            }
            if (!membership.get().getBusinessId().equals(businessId)) {
                throw new InvariantViolationException(
                        "Membership does not belong to business: " + command.businessId());
            }
            // One worker profile per membership within a business (ADR-013 §8.6)
            Optional<Worker> byMembership = workerRepository.findByMembershipId(membershipId);
            if (byMembership.isPresent()) {
                return toResult(byMembership.get(), true);
            }
        }

        Optional<StableId> clientId = command.workerId().map(StableId::fromString);

        if (clientId.isPresent()) {
            Optional<Worker> byId = workerRepository.findById(clientId.get());
            if (byId.isPresent()) {
                return toResult(byId.get(), true);
            }
        }

        Worker created = clientId.isPresent()
                ? Worker.create(clientId.get(), businessId, name, membershipId, Instant.now())
                : Worker.create(businessId, name, membershipId);
        Worker saved = workerRepository.save(created);
        saved.pullDomainEvents();
        return toResult(saved, false);
    }

    private static CreateWorkerResult toResult(Worker worker, boolean alreadyExisted) {
        return new CreateWorkerResult(
                worker.getId().toString(),
                worker.getBusinessId().toString(),
                worker.getName().asString(),
                worker.getMembershipId().map(StableId::toString).orElse(null),
                worker.getStatus().name(),
                worker.getCreatedAt(),
                alreadyExisted);
    }
}
