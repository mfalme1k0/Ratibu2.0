package com.ratibu.catalog.application;

import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.catalog.domain.CatalogService;
import com.ratibu.catalog.domain.CatalogServiceRepository;
import com.ratibu.catalog.domain.ServiceName;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Creates a business-level catalog service (ADR-004 §14).
 * Shop-specific pricing/availability is deferred.
 */
@Service
public class CreateCatalogServiceService {

    private final CatalogServiceRepository catalogServiceRepository;
    private final BusinessRepository businessRepository;

    public CreateCatalogServiceService(
            CatalogServiceRepository catalogServiceRepository,
            BusinessRepository businessRepository) {
        this.catalogServiceRepository = catalogServiceRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public CreateCatalogServiceResult execute(CreateCatalogServiceCommand command) {
        StableId businessId = StableId.fromString(command.businessId());
        ServiceName name = ServiceName.of(command.name());

        if (!businessRepository.existsById(businessId)) {
            throw new NotFoundException("Business not found: " + command.businessId());
        }

        if (command.defaultPrice() == null) {
            throw new InvariantViolationException("defaultPrice is required");
        }
        Money defaultPrice = Money.of(command.defaultPrice());

        String description = command.description().filter(d -> !d.isBlank()).orElse(null);

        Optional<StableId> clientId = command.serviceId().map(StableId::fromString);
        if (clientId.isPresent()) {
            Optional<CatalogService> existing = catalogServiceRepository.findById(clientId.get());
            if (existing.isPresent()) {
                return toResult(existing.get(), true);
            }
        }

        CatalogService created = clientId.isPresent()
                ? CatalogService.create(
                        clientId.get(), businessId, name, description, defaultPrice, Instant.now())
                : CatalogService.create(businessId, name, description, defaultPrice);
        CatalogService saved = catalogServiceRepository.save(created);
        saved.pullDomainEvents();
        return toResult(saved, false);
    }

    private static CreateCatalogServiceResult toResult(CatalogService service, boolean alreadyExisted) {
        return new CreateCatalogServiceResult(
                service.getId().toString(),
                service.getBusinessId().toString(),
                service.getName().asString(),
                service.getDescription().orElse(null),
                service.getDefaultPrice().asBigDecimal(),
                service.getStatus().name(),
                service.getCreatedAt(),
                alreadyExisted);
    }
}
