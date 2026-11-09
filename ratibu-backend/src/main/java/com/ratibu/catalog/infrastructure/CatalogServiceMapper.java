package com.ratibu.catalog.infrastructure;

import com.ratibu.catalog.domain.CatalogService;
import com.ratibu.catalog.domain.ServiceName;
import com.ratibu.catalog.domain.ServiceStatus;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;

final class CatalogServiceMapper {

    private CatalogServiceMapper() {
    }

    static CatalogServiceJpaEntity toEntity(CatalogService service) {
        return new CatalogServiceJpaEntity(
                service.getId().asUuid(),
                service.getBusinessId().asUuid(),
                service.getName().asString(),
                service.getDescription().orElse(null),
                service.getDefaultPrice().asBigDecimal(),
                service.getStatus().name(),
                service.getCreatedAt());
    }

    static void updateEntity(CatalogServiceJpaEntity entity, CatalogService service) {
        entity.setName(service.getName().asString());
        entity.setDescription(service.getDescription().orElse(null));
        entity.setDefaultPrice(service.getDefaultPrice().asBigDecimal());
        entity.setStatus(service.getStatus().name());
    }

    static CatalogService toDomain(CatalogServiceJpaEntity entity) {
        return CatalogService.reconstitute(
                StableId.fromUuid(entity.getId()),
                StableId.fromUuid(entity.getBusinessId()),
                ServiceName.of(entity.getName()),
                entity.getDescription(),
                Money.of(entity.getDefaultPrice()),
                ServiceStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt());
    }
}
