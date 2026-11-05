package com.ratibu.shop.application;

import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.NotFoundException;
import com.ratibu.shop.domain.Shop;
import com.ratibu.shop.domain.ShopName;
import com.ratibu.shop.domain.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Creates a Shop under an existing Business (ADR-003, ADR-012 §18–19).
 * Coordinates via repository ports only — does not load Business internals.
 */
@Service
public class CreateShopService {

    private final ShopRepository shopRepository;
    private final BusinessRepository businessRepository;

    public CreateShopService(ShopRepository shopRepository, BusinessRepository businessRepository) {
        this.shopRepository = shopRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public CreateShopResult execute(CreateShopCommand command) {
        StableId businessId = StableId.fromString(command.businessId());
        if (!businessRepository.existsById(businessId)) {
            throw new NotFoundException("Business not found: " + command.businessId());
        }

        ShopName name = ShopName.of(command.name());
        Optional<StableId> clientId = command.shopId().map(StableId::fromString);

        if (clientId.isPresent()) {
            StableId id = clientId.get();
            Optional<Shop> existing = shopRepository.findById(id);
            if (existing.isPresent()) {
                return toResult(existing.get(), true);
            }
            Shop created = Shop.create(id, businessId, name, Instant.now());
            Shop saved = shopRepository.save(created);
            saved.pullDomainEvents();
            return toResult(saved, false);
        }

        Shop created = Shop.create(businessId, name);
        Shop saved = shopRepository.save(created);
        saved.pullDomainEvents();
        return toResult(saved, false);
    }

    private static CreateShopResult toResult(Shop shop, boolean alreadyExisted) {
        return new CreateShopResult(
                shop.getId().toString(),
                shop.getBusinessId().toString(),
                shop.getName().asString(),
                shop.getStatus().name(),
                shop.getCreatedAt(),
                alreadyExisted);
    }
}
