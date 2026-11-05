package com.ratibu.shop.application;

import com.ratibu.common.domain.StableId;
import com.ratibu.shop.domain.Shop;
import com.ratibu.shop.domain.ShopRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class InMemoryShopRepository implements ShopRepository {

    private final Map<StableId, Shop> store = new ConcurrentHashMap<>();

    @Override
    public Shop save(Shop shop) {
        Shop snapshot = Shop.reconstitute(
                shop.getId(),
                shop.getBusinessId(),
                shop.getName(),
                shop.getStatus(),
                shop.getCreatedAt());
        store.put(snapshot.getId(), snapshot);
        return snapshot;
    }

    @Override
    public Optional<Shop> findById(StableId id) {
        return Optional.ofNullable(store.get(id)).map(s -> Shop.reconstitute(
                s.getId(), s.getBusinessId(), s.getName(), s.getStatus(), s.getCreatedAt()));
    }

    @Override
    public boolean existsById(StableId id) {
        return store.containsKey(id);
    }

    @Override
    public List<Shop> findByBusinessId(StableId businessId) {
        return store.values().stream()
                .filter(s -> s.getBusinessId().equals(businessId))
                .map(s -> Shop.reconstitute(
                        s.getId(), s.getBusinessId(), s.getName(), s.getStatus(), s.getCreatedAt()))
                .toList();
    }
}
