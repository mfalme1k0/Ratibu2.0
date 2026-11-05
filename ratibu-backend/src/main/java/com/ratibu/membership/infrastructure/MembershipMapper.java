package com.ratibu.membership.infrastructure;

import com.ratibu.common.domain.StableId;
import com.ratibu.membership.domain.Membership;
import com.ratibu.membership.domain.MembershipRole;

final class MembershipMapper {

    private MembershipMapper() {
    }

    static MembershipJpaEntity toEntity(Membership membership) {
        return new MembershipJpaEntity(
                membership.getId().asUuid(),
                membership.getAccountId().asUuid(),
                membership.getBusinessId().asUuid(),
                membership.getRole().name(),
                membership.getCreatedAt());
    }

    static void updateEntity(MembershipJpaEntity entity, Membership membership) {
        entity.setRole(membership.getRole().name());
    }

    static Membership toDomain(MembershipJpaEntity entity) {
        return Membership.reconstitute(
                StableId.fromUuid(entity.getId()),
                StableId.fromUuid(entity.getAccountId()),
                StableId.fromUuid(entity.getBusinessId()),
                MembershipRole.valueOf(entity.getRole()),
                entity.getCreatedAt());
    }
}
