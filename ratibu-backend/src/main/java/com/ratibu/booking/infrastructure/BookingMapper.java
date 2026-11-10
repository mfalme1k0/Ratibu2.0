package com.ratibu.booking.infrastructure;

import com.ratibu.booking.domain.Booking;
import com.ratibu.booking.domain.BookingStatus;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;

final class BookingMapper {

    private BookingMapper() {
    }

    static BookingJpaEntity toEntity(Booking booking) {
        return new BookingJpaEntity(
                booking.getId().asUuid(),
                booking.getShopId().asUuid(),
                booking.getClientId().asUuid(),
                booking.getServiceId().asUuid(),
                booking.getWorkerId().map(StableId::asUuid).orElse(null),
                booking.getScheduledAt(),
                booking.getDurationMinutes().orElse(null),
                booking.getExpectedPrice().asBigDecimal(),
                booking.getFinalPrice().map(Money::asBigDecimal).orElse(null),
                booking.getStatus().name(),
                booking.getCreatedAt());
    }

    static void updateEntity(BookingJpaEntity entity, Booking booking) {
        entity.setWorkerId(booking.getWorkerId().map(StableId::asUuid).orElse(null));
        entity.setScheduledAt(booking.getScheduledAt());
        entity.setDurationMinutes(booking.getDurationMinutes().orElse(null));
        entity.setFinalPrice(booking.getFinalPrice().map(Money::asBigDecimal).orElse(null));
        entity.setStatus(booking.getStatus().name());
    }

    static Booking toDomain(BookingJpaEntity entity) {
        return Booking.reconstitute(
                StableId.fromUuid(entity.getId()),
                StableId.fromUuid(entity.getShopId()),
                StableId.fromUuid(entity.getClientId()),
                StableId.fromUuid(entity.getServiceId()),
                entity.getWorkerId() != null ? StableId.fromUuid(entity.getWorkerId()) : null,
                entity.getScheduledAt(),
                entity.getDurationMinutes(),
                Money.of(entity.getExpectedPrice()),
                entity.getFinalPrice() != null ? Money.of(entity.getFinalPrice()) : null,
                BookingStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt());
    }
}
