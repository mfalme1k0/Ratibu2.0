package com.ratibu.shop.infrastructure;

import com.ratibu.common.domain.StableId;
import com.ratibu.shop.domain.Shop;
import com.ratibu.shop.domain.ShopName;
import com.ratibu.shop.domain.ShopStatus;

final class ShopMapper {

    private ShopMapper() {
    }

    static ShopJpaEntity toEntity(Shop shop) {
        return new ShopJpaEntity(
                shop.getId().asUuid(),
                shop.getBusinessId().asUuid(),
                shop.getName().asString(),
                shop.getStatus().name(),
                shop.getCreatedAt());
    }

    static void updateEntity(ShopJpaEntity entity, Shop shop) {
        entity.setName(shop.getName().asString());
        entity.setStatus(shop.getStatus().name());
    }

    static Shop toDomain(ShopJpaEntity entity) {
        return Shop.reconstitute(
                StableId.fromUuid(entity.getId()),
                StableId.fromUuid(entity.getBusinessId()),
                ShopName.of(entity.getName()),
                ShopStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt());
    }
}
