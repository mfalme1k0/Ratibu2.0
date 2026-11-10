package com.ratibu.booking.domain;

import com.ratibu.common.domain.StableId;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(StableId id);

    List<Booking> findByShopId(StableId shopId);

    List<Booking> findByClientId(StableId clientId);

    boolean existsById(StableId id);
}
