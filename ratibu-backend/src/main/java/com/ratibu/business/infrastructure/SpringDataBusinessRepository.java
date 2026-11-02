package com.ratibu.business.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data store for {@link BusinessJpaEntity}. Not used outside infrastructure;
 * domain code talks to {@link com.ratibu.business.domain.BusinessRepository}.
 */
interface SpringDataBusinessRepository extends JpaRepository<BusinessJpaEntity, UUID> {
}
