package com.ratibu.business.domain;

import com.ratibu.common.domain.StableId;

import java.util.Optional;

/**
 * Persistence port for the Business aggregate. Defined in the domain layer;
 * implemented in infrastructure (ADR-012 §18). Callers depend on this
 * interface, never on JPA types.
 */
public interface BusinessRepository {

    Business save(Business business);

    Optional<Business> findById(StableId id);

    boolean existsById(StableId id);
}
