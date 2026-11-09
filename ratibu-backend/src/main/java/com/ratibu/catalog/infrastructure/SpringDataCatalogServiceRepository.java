package com.ratibu.catalog.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataCatalogServiceRepository extends JpaRepository<CatalogServiceJpaEntity, UUID> {

    List<CatalogServiceJpaEntity> findByBusinessId(UUID businessId);
}
