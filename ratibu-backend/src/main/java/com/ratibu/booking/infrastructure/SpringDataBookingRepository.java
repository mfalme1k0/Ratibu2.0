package com.ratibu.booking.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, UUID> {

    List<BookingJpaEntity> findByShopId(UUID shopId);

    List<BookingJpaEntity> findByClientId(UUID clientId);
}
