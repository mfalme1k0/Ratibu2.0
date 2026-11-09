package com.ratibu.catalog.domain;

import com.ratibu.common.domain.StableId;

import java.util.List;
import java.util.Optional;

public interface CatalogServiceRepository {

    CatalogService save(CatalogService service);

    Optional<CatalogService> findById(StableId id);

    List<CatalogService> findByBusinessId(StableId businessId);

    boolean existsById(StableId id);
}
