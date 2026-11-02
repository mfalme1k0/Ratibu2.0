package com.ratibu.business.infrastructure;

import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.common.domain.StableId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Infrastructure adapter implementing the domain {@link BusinessRepository}
 * port with Spring Data JPA.
 */
@Repository
@Transactional
public class BusinessRepositoryAdapter implements BusinessRepository {

    private final SpringDataBusinessRepository springData;

    public BusinessRepositoryAdapter(SpringDataBusinessRepository springData) {
        this.springData = springData;
    }

    @Override
    public Business save(Business business) {
        Optional<BusinessJpaEntity> existing = springData.findById(business.getId().asUuid());
        BusinessJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            BusinessMapper.updateEntity(entity, business);
        } else {
            entity = BusinessMapper.toEntity(business);
        }
        BusinessJpaEntity saved = springData.save(entity);
        return BusinessMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Business> findById(StableId id) {
        return springData.findById(id.asUuid()).map(BusinessMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(StableId id) {
        return springData.existsById(id.asUuid());
    }
}
