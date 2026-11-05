package com.ratibu.shop.infrastructure;

import com.ratibu.common.domain.StableId;
import com.ratibu.shop.domain.Shop;
import com.ratibu.shop.domain.ShopRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ShopRepositoryAdapter implements ShopRepository {

    private final SpringDataShopRepository springData;

    public ShopRepositoryAdapter(SpringDataShopRepository springData) {
        this.springData = springData;
    }

    @Override
    public Shop save(Shop shop) {
        Optional<ShopJpaEntity> existing = springData.findById(shop.getId().asUuid());
        ShopJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            ShopMapper.updateEntity(entity, shop);
        } else {
            entity = ShopMapper.toEntity(shop);
        }
        return ShopMapper.toDomain(springData.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shop> findById(StableId id) {
        return springData.findById(id.asUuid()).map(ShopMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(StableId id) {
        return springData.existsById(id.asUuid());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shop> findByBusinessId(StableId businessId) {
        return springData.findByBusinessId(businessId.asUuid()).stream()
                .map(ShopMapper::toDomain)
                .toList();
    }
}
