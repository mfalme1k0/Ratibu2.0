package com.ratibu.booking.infrastructure;

import com.ratibu.booking.domain.Booking;
import com.ratibu.booking.domain.BookingRepository;
import com.ratibu.common.domain.StableId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class BookingRepositoryAdapter implements BookingRepository {

    private final SpringDataBookingRepository springData;

    public BookingRepositoryAdapter(SpringDataBookingRepository springData) {
        this.springData = springData;
    }

    @Override
    public Booking save(Booking booking) {
        Optional<BookingJpaEntity> existing = springData.findById(booking.getId().asUuid());
        BookingJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            BookingMapper.updateEntity(entity, booking);
        } else {
            entity = BookingMapper.toEntity(booking);
        }
        return BookingMapper.toDomain(springData.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findById(StableId id) {
        return springData.findById(id.asUuid()).map(BookingMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByShopId(StableId shopId) {
        return springData.findByShopId(shopId.asUuid()).stream()
                .map(BookingMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByClientId(StableId clientId) {
        return springData.findByClientId(clientId.asUuid()).stream()
                .map(BookingMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(StableId id) {
        return springData.existsById(id.asUuid());
    }
}
