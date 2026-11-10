package com.ratibu.booking.application;

import com.ratibu.booking.domain.Booking;
import com.ratibu.booking.domain.BookingRepository;
import com.ratibu.common.domain.StableId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryBookingRepository implements BookingRepository {

    private final Map<StableId, Booking> store = new ConcurrentHashMap<>();

    @Override
    public Booking save(Booking booking) {
        Booking snap = copy(booking);
        store.put(snap.getId(), snap);
        return snap;
    }

    @Override
    public Optional<Booking> findById(StableId id) {
        return Optional.ofNullable(store.get(id)).map(this::copy);
    }

    @Override
    public List<Booking> findByShopId(StableId shopId) {
        return store.values().stream().filter(b -> b.getShopId().equals(shopId)).map(this::copy).toList();
    }

    @Override
    public List<Booking> findByClientId(StableId clientId) {
        return store.values().stream().filter(b -> b.getClientId().equals(clientId)).map(this::copy).toList();
    }

    @Override
    public boolean existsById(StableId id) {
        return store.containsKey(id);
    }

    private Booking copy(Booking b) {
        return Booking.reconstitute(
                b.getId(), b.getShopId(), b.getClientId(), b.getServiceId(),
                b.getWorkerId().orElse(null), b.getScheduledAt(),
                b.getDurationMinutes().orElse(null), b.getExpectedPrice(),
                b.getFinalPrice().orElse(null), b.getStatus(), b.getCreatedAt());
    }
}
