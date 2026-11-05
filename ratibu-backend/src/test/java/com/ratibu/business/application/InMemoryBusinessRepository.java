package com.ratibu.business.application;

import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.common.domain.StableId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test double for {@link BusinessRepository}. Stores reconstituted aggregates
 * so load behaviour matches production (no leftover domain events).
 */
public final class InMemoryBusinessRepository implements BusinessRepository {

    private final Map<StableId, Business> store = new ConcurrentHashMap<>();

    @Override
    public Business save(Business business) {
        Business snapshot = Business.reconstitute(
                business.getId(),
                business.getName(),
                business.getCurrency(),
                business.getCreatedAt());
        store.put(snapshot.getId(), snapshot);
        return snapshot;
    }

    @Override
    public Optional<Business> findById(StableId id) {
        return Optional.ofNullable(store.get(id)).map(b -> Business.reconstitute(
                b.getId(),
                b.getName(),
                b.getCurrency(),
                b.getCreatedAt()));
    }

    @Override
    public boolean existsById(StableId id) {
        return store.containsKey(id);
    }

    int size() {
        return store.size();
    }
}
