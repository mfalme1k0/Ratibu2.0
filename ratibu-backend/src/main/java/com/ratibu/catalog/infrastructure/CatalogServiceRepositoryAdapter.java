package com.ratibu.catalog.infrastructure;

import com.ratibu.catalog.domain.CatalogService;
import com.ratibu.catalog.domain.CatalogServiceRepository;
import com.ratibu.common.domain.StableId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CatalogServiceRepositoryAdapter implements CatalogServiceRepository {

    private final SpringDataCatalogServiceRepository springData;

    public CatalogServiceRepositoryAdapter(SpringDataCatalogServiceRepository springData) {
        this.springData = springData;
    }

    @Override
    public CatalogService save(CatalogService service) {
        Optional<CatalogServiceJpaEntity> existing = springData.findById(service.getId().asUuid());
        CatalogServiceJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            CatalogServiceMapper.updateEntity(entity, service);
        } else {
            entity = CatalogServiceMapper.toEntity(service);
        }
        return CatalogServiceMapper.toDomain(springData.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CatalogService> findById(StableId id) {
        return springData.findById(id.asUuid()).map(CatalogServiceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogService> findByBusinessId(StableId businessId) {
        return springData.findByBusinessId(businessId.asUuid()).stream()
                .map(CatalogServiceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(StableId id) {
        return springData.existsById(id.asUuid());
    }
}
