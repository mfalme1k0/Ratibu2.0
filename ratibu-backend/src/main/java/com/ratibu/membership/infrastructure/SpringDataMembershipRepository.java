package com.ratibu.membership.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataMembershipRepository extends JpaRepository<MembershipJpaEntity, UUID> {

    Optional<MembershipJpaEntity> findByAccountIdAndBusinessId(UUID accountId, UUID businessId);

    List<MembershipJpaEntity> findByBusinessId(UUID businessId);

    List<MembershipJpaEntity> findByAccountId(UUID accountId);
}
