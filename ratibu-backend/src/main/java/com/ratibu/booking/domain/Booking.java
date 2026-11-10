package com.ratibu.booking.domain;

import com.ratibu.common.domain.AggregateRoot;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Booking — operational schedule/lifecycle entity, independent of Payment
 * (ADR-005, ADR-013 §4–§6).
 *
 * <p>Owns: shop, client, service, optional worker, scheduled time, optional duration,
 * expected price (snapshot at create), optional final price, status lifecycle.
 *
 * <p>Does not create income. Payment is a separate aggregate.
 */
public final class Booking extends AggregateRoot {

    private final StableId shopId;
    private final StableId clientId;
    private final StableId serviceId;
    private StableId workerId;
    private Instant scheduledAt;
    private Integer durationMinutes;
    private final Money expectedPrice;
    private Money finalPrice;
    private BookingStatus status;
    private final Instant createdAt;

    private Booking(
            StableId id,
            StableId shopId,
            StableId clientId,
            StableId serviceId,
            StableId workerId,
            Instant scheduledAt,
            Integer durationMinutes,
            Money expectedPrice,
            Money finalPrice,
            BookingStatus status,
            Instant createdAt) {
        super(id);
        this.shopId = Objects.requireNonNull(shopId);
        this.clientId = Objects.requireNonNull(clientId);
        this.serviceId = Objects.requireNonNull(serviceId);
        this.workerId = workerId;
        this.scheduledAt = Objects.requireNonNull(scheduledAt);
        this.durationMinutes = durationMinutes;
        this.expectedPrice = Objects.requireNonNull(expectedPrice);
        this.finalPrice = finalPrice;
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Booking create(
            StableId shopId,
            StableId clientId,
            StableId serviceId,
            StableId workerId,
            Instant scheduledAt,
            Integer durationMinutes,
            Money expectedPrice) {
        return create(
                StableId.generate(), shopId, clientId, serviceId, workerId,
                scheduledAt, durationMinutes, expectedPrice, Instant.now());
    }

    public static Booking create(
            StableId id,
            StableId shopId,
            StableId clientId,
            StableId serviceId,
            StableId workerId,
            Instant scheduledAt,
            Integer durationMinutes,
            Money expectedPrice,
            Instant createdAt) {
        if (id == null) {
            throw new InvariantViolationException("Booking id must not be null");
        }
        if (shopId == null) {
            throw new InvariantViolationException("Booking shopId must not be null");
        }
        if (clientId == null) {
            throw new InvariantViolationException("Booking clientId must not be null");
        }
        if (serviceId == null) {
            throw new InvariantViolationException("Booking serviceId must not be null");
        }
        if (scheduledAt == null) {
            throw new InvariantViolationException("Booking scheduledAt must not be null");
        }
        if (expectedPrice == null) {
            throw new InvariantViolationException("Booking expectedPrice must not be null");
        }
        if (expectedPrice.isNegative()) {
            throw new InvariantViolationException("Booking expectedPrice must not be negative");
        }
        if (durationMinutes != null && durationMinutes <= 0) {
            throw new InvariantViolationException("Booking durationMinutes must be positive when set");
        }
        Booking booking = new Booking(
                id, shopId, clientId, serviceId, workerId, scheduledAt, durationMinutes,
                expectedPrice, null, BookingStatus.SCHEDULED, createdAt);
        booking.raise(new BookingCreated(
                id, shopId, clientId, serviceId, expectedPrice, scheduledAt, createdAt));
        return booking;
    }

    public static Booking reconstitute(
            StableId id,
            StableId shopId,
            StableId clientId,
            StableId serviceId,
            StableId workerId,
            Instant scheduledAt,
            Integer durationMinutes,
            Money expectedPrice,
            Money finalPrice,
            BookingStatus status,
            Instant createdAt) {
        return new Booking(
                id, shopId, clientId, serviceId, workerId, scheduledAt, durationMinutes,
                expectedPrice, finalPrice, status, createdAt);
    }

    public StableId getShopId() {
        return shopId;
    }

    public StableId getClientId() {
        return clientId;
    }

    public StableId getServiceId() {
        return serviceId;
    }

    public Optional<StableId> getWorkerId() {
        return Optional.ofNullable(workerId);
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public Optional<Integer> getDurationMinutes() {
        return Optional.ofNullable(durationMinutes);
    }

    public Money getExpectedPrice() {
        return expectedPrice;
    }

    public Optional<Money> getFinalPrice() {
        return Optional.ofNullable(finalPrice);
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Amount due for payment purposes: final price if set, else expected (ADR-013 §6). */
    public Money amountDue() {
        return finalPrice != null ? finalPrice : expectedPrice;
    }

    public void assignWorker(StableId workerId) {
        requireStatus(BookingStatus.SCHEDULED, "assign worker");
        if (workerId == null) {
            throw new InvariantViolationException("Worker id must not be null");
        }
        this.workerId = workerId;
    }

    public void reschedule(Instant newScheduledAt) {
        requireStatus(BookingStatus.SCHEDULED, "reschedule");
        if (newScheduledAt == null) {
            throw new InvariantViolationException("scheduledAt must not be null");
        }
        this.scheduledAt = newScheduledAt;
    }

    public void start() {
        requireStatus(BookingStatus.SCHEDULED, "start");
        this.status = BookingStatus.IN_PROGRESS;
    }

    /**
     * Completes the service. Optional final price defaults to expected price when omitted
     * (ADR-013 §6). Does not imply payment received (ADR-005 §15).
     */
    public void complete(Money finalPriceOrNull) {
        if (status != BookingStatus.IN_PROGRESS && status != BookingStatus.SCHEDULED) {
            throw new InvariantViolationException(
                    "Cannot complete booking in status " + status);
        }
        if (finalPriceOrNull != null && finalPriceOrNull.isNegative()) {
            throw new InvariantViolationException("finalPrice must not be negative");
        }
        this.finalPrice = finalPriceOrNull != null ? finalPriceOrNull : this.expectedPrice;
        this.status = BookingStatus.COMPLETED;
    }

    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            return;
        }
        if (status != BookingStatus.SCHEDULED) {
            throw new InvariantViolationException(
                    "Cannot cancel booking in status " + status);
        }
        this.status = BookingStatus.CANCELLED;
    }

    public void markNoShow() {
        if (status == BookingStatus.NO_SHOW) {
            return;
        }
        if (status != BookingStatus.SCHEDULED) {
            throw new InvariantViolationException(
                    "Cannot mark no-show for booking in status " + status);
        }
        this.status = BookingStatus.NO_SHOW;
    }

    private void requireStatus(BookingStatus required, String action) {
        if (status != required) {
            throw new InvariantViolationException(
                    "Cannot " + action + " when booking status is " + status);
        }
    }
}
