package com.ratibu.membership.domain;

import com.ratibu.common.domain.StableId;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository {

    Membership save(Membership membership);

    Optional<Membership> findById(StableId id);

    Optional<Membership> findByAccountIdAndBusinessId(StableId accountId, StableId businessId);

    List<Membership> findByBusinessId(StableId businessId);

    List<Membership> findByAccountId(StableId accountId);

    boolean existsById(StableId id);
}
