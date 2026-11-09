package com.ratibu.catalog.application;

import com.ratibu.catalog.domain.CatalogService;
import com.ratibu.catalog.domain.CatalogServiceRepository;
import com.ratibu.common.domain.StableId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCatalogServiceRepository implements CatalogServiceRepository {

    private final Map<StableId, CatalogService> store = new ConcurrentHashMap<>();

    @Override
    public CatalogService save(CatalogService service) {
        CatalogService snap = CatalogService.reconstitute(
                service.getId(),
                service.getBusinessId(),
                service.getName(),
                service.getDescription().orElse(null),
                service.getDefaultPrice(),
                service.getStatus(),
                service.getCreatedAt());
        store.put(snap.getId(), snap);
        return snap;
    }

    @Override
    public Optional<CatalogService> findById(StableId id) {
        return Optional.ofNullable(store.get(id)).map(this::copy);
    }

    @Override
    public List<CatalogService> findByBusinessId(StableId businessId) {
        return store.values().stream()
                .filter(s -> s.getBusinessId().equals(businessId))
                .map(this::copy)
                .toList();
    }

    @Override
    public boolean existsById(StableId id) {
        return store.containsKey(id);
    }

    private CatalogService copy(CatalogService s) {
        return CatalogService.reconstitute(
                s.getId(),
                s.getBusinessId(),
                s.getName(),
                s.getDescription().orElse(null),
                s.getDefaultPrice(),
                s.getStatus(),
                s.getCreatedAt());
    }
}
