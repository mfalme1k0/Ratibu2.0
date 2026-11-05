package com.ratibu.membership.application;

import com.ratibu.common.domain.StableId;
import com.ratibu.membership.domain.Membership;
import com.ratibu.membership.domain.MembershipRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryMembershipRepository implements MembershipRepository {

    private final Map<StableId, Membership> store = new ConcurrentHashMap<>();

    @Override
    public Membership save(Membership membership) {
        Membership snap = Membership.reconstitute(
                membership.getId(),
                membership.getAccountId(),
                membership.getBusinessId(),
                membership.getRole(),
                membership.getCreatedAt());
        store.put(snap.getId(), snap);
        return snap;
    }

    @Override
    public Optional<Membership> findById(StableId id) {
        return Optional.ofNullable(store.get(id)).map(this::copy);
    }

    @Override
    public Optional<Membership> findByAccountIdAndBusinessId(StableId accountId, StableId businessId) {
        return store.values().stream()
                .filter(m -> m.getAccountId().equals(accountId) && m.getBusinessId().equals(businessId))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public List<Membership> findByBusinessId(StableId businessId) {
        return store.values().stream()
                .filter(m -> m.getBusinessId().equals(businessId))
                .map(this::copy)
                .toList();
    }

    @Override
    public List<Membership> findByAccountId(StableId accountId) {
        return store.values().stream()
                .filter(m -> m.getAccountId().equals(accountId))
                .map(this::copy)
                .toList();
    }

    @Override
    public boolean existsById(StableId id) {
        return store.containsKey(id);
    }

    private Membership copy(Membership m) {
        return Membership.reconstitute(
                m.getId(), m.getAccountId(), m.getBusinessId(), m.getRole(), m.getCreatedAt());
    }
}
