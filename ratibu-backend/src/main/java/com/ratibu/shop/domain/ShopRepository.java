package com.ratibu.shop.domain;

import com.ratibu.common.domain.StableId;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for the Shop aggregate (ADR-012 §18).
 */
public interface ShopRepository {

    Shop save(Shop shop);

    Optional<Shop> findById(StableId id);

    boolean existsById(StableId id);

    List<Shop> findByBusinessId(StableId businessId);
}
