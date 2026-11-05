package com.ratibu.membership.infrastructure;

import com.ratibu.common.domain.StableId;
import com.ratibu.membership.domain.Membership;
import com.ratibu.membership.domain.MembershipRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class MembershipRepositoryAdapter implements MembershipRepository {

    private final SpringDataMembershipRepository springData;

    public MembershipRepositoryAdapter(SpringDataMembershipRepository springData) {
        this.springData = springData;
    }

    @Override
    public Membership save(Membership membership) {
        Optional<MembershipJpaEntity> existing = springData.findById(membership.getId().asUuid());
        MembershipJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            MembershipMapper.updateEntity(entity, membership);
        } else {
            entity = MembershipMapper.toEntity(membership);
        }
        return MembershipMapper.toDomain(springData.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Membership> findById(StableId id) {
        return springData.findById(id.asUuid()).map(MembershipMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Membership> findByAccountIdAndBusinessId(StableId accountId, StableId businessId) {
        return springData.findByAccountIdAndBusinessId(accountId.asUuid(), businessId.asUuid())
                .map(MembershipMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Membership> findByBusinessId(StableId businessId) {
        return springData.findByBusinessId(businessId.asUuid()).stream()
                .map(MembershipMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Membership> findByAccountId(StableId accountId) {
        return springData.findByAccountId(accountId.asUuid()).stream()
                .map(MembershipMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(StableId id) {
        return springData.existsById(id.asUuid());
    }
}
