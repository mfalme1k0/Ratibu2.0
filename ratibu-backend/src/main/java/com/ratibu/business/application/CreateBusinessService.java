package com.ratibu.business.application;

import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.common.domain.StableId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Application service for creating a Business (ADR-012 §18).
 *
 * <p>Orchestration only: maps command → domain types, enforces idempotent
 * create when a client StableId is supplied (ADR-010), persists via the
 * domain port, and returns a result. Does not touch other aggregates;
 * reactions to {@link com.ratibu.business.domain.BusinessCreated} belong in
 * separate application services (e.g. membership bootstrap).
 */
@Service
public class CreateBusinessService {

    private final BusinessRepository businessRepository;

    public CreateBusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @Transactional
    public CreateBusinessResult execute(CreateBusinessCommand command) {
        BusinessName name = BusinessName.of(command.name());
        CurrencyCode currency = CurrencyCode.of(command.currency());

        Optional<StableId> clientId = command.businessId().map(StableId::fromString);

        if (clientId.isPresent()) {
            StableId id = clientId.get();
            Optional<Business> existing = businessRepository.findById(id);
            if (existing.isPresent()) {
                return toResult(existing.get(), true);
            }
            Business created = Business.create(id, name, currency, Instant.now());
            Business saved = businessRepository.save(created);
            // Drain events so they are available to a future event publisher
            // without leaking into the returned aggregate state.
            saved.pullDomainEvents();
            return toResult(saved, false);
        }

        Business created = Business.create(name, currency);
        Business saved = businessRepository.save(created);
        saved.pullDomainEvents();
        return toResult(saved, false);
    }

    private static CreateBusinessResult toResult(Business business, boolean alreadyExisted) {
        return new CreateBusinessResult(
                business.getId().toString(),
                business.getName().asString(),
                business.getCurrency().asString(),
                business.getCreatedAt(),
                alreadyExisted);
    }
}
