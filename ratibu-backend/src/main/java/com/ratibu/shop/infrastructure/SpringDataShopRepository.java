package com.ratibu.shop.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataShopRepository extends JpaRepository<ShopJpaEntity, UUID> {

    List<ShopJpaEntity> findByBusinessId(UUID businessId);
}
