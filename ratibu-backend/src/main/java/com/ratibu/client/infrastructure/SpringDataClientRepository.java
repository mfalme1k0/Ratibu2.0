package com.ratibu.client.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataClientRepository extends JpaRepository<ClientJpaEntity, UUID> {

    List<ClientJpaEntity> findByBusinessId(UUID businessId);

    List<ClientJpaEntity> findByBusinessIdAndPhone(UUID businessId, String phone);
}
