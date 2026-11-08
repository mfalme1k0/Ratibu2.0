package com.ratibu.client.application;

import com.ratibu.client.domain.Client;
import com.ratibu.client.domain.ClientRepository;
import com.ratibu.client.domain.PhoneNumber;
import com.ratibu.common.domain.StableId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryClientRepository implements ClientRepository {

    private final Map<StableId, Client> store = new ConcurrentHashMap<>();

    @Override
    public Client save(Client client) {
        Client snap = Client.reconstitute(
                client.getId(),
                client.getBusinessId(),
                client.getName(),
                client.getPhone().orElse(null),
                client.getEmail().orElse(null),
                client.getNotes().orElse(null),
                client.getStatus(),
                client.getCreatedAt());
        store.put(snap.getId(), snap);
        return snap;
    }

    @Override
    public Optional<Client> findById(StableId id) {
        return Optional.ofNullable(store.get(id)).map(this::copy);
    }

    @Override
    public List<Client> findByBusinessId(StableId businessId) {
        return store.values().stream()
                .filter(c -> c.getBusinessId().equals(businessId))
                .map(this::copy)
                .toList();
    }

    @Override
    public List<Client> findByBusinessIdAndPhone(StableId businessId, PhoneNumber phone) {
        return store.values().stream()
                .filter(c -> c.getBusinessId().equals(businessId)
                        && c.getPhone().isPresent()
                        && c.getPhone().get().equals(phone))
                .map(this::copy)
                .toList();
    }

    @Override
    public boolean existsById(StableId id) {
        return store.containsKey(id);
    }

    private Client copy(Client c) {
        return Client.reconstitute(
                c.getId(),
                c.getBusinessId(),
                c.getName(),
                c.getPhone().orElse(null),
                c.getEmail().orElse(null),
                c.getNotes().orElse(null),
                c.getStatus(),
                c.getCreatedAt());
    }
}
