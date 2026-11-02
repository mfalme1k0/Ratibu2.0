package com.ratibu.business.infrastructure;

import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.common.domain.StableId;

/**
 * Maps between the domain {@link Business} aggregate and the JPA entity.
 * Uses {@link Business#reconstitute} on the way in so load does not raise
 * domain events.
 */
final class BusinessMapper {

    private BusinessMapper() {
    }

    static BusinessJpaEntity toEntity(Business business) {
        return new BusinessJpaEntity(
                business.getId().asUuid(),
                business.getName().asString(),
                business.getCurrency().asString(),
                business.getCreatedAt());
    }

    static void updateEntity(BusinessJpaEntity entity, Business business) {
        // Currency and id are immutable after create; only name can change in MVP.
        entity.setName(business.getName().asString());
    }

    static Business toDomain(BusinessJpaEntity entity) {
        return Business.reconstitute(
                StableId.fromUuid(entity.getId()),
                BusinessName.of(entity.getName()),
                CurrencyCode.of(entity.getCurrency()),
                entity.getCreatedAt());
    }
}
