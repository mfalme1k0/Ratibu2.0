package com.ratibu.client.domain;

import com.ratibu.common.domain.StableId;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {

    Client save(Client client);

    Optional<Client> findById(StableId id);

    List<Client> findByBusinessId(StableId businessId);

    /**
     * Soft-match helper for the future manual merge queue (ADR-013 §7).
     * Does not imply uniqueness — multiple clients may share a phone.
     */
    List<Client> findByBusinessIdAndPhone(StableId businessId, PhoneNumber phone);

    boolean existsById(StableId id);
}
