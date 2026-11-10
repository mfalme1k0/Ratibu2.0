package com.ratibu.booking.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking")
public class BookingJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "shop_id", nullable = false, updatable = false)
    private UUID shopId;

    @Column(name = "client_id", nullable = false, updatable = false)
    private UUID clientId;

    @Column(name = "service_id", nullable = false, updatable = false)
    private UUID serviceId;

    @Column(name = "worker_id")
    private UUID workerId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "expected_price", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal expectedPrice;

    @Column(name = "final_price", precision = 19, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BookingJpaEntity() {
    }

    public BookingJpaEntity(
            UUID id,
            UUID shopId,
            UUID clientId,
            UUID serviceId,
            UUID workerId,
            Instant scheduledAt,
            Integer durationMinutes,
            BigDecimal expectedPrice,
            BigDecimal finalPrice,
            String status,
            Instant createdAt) {
        this.id = id;
        this.shopId = shopId;
        this.clientId = clientId;
        this.serviceId = serviceId;
        this.workerId = workerId;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.expectedPrice = expectedPrice;
        this.finalPrice = finalPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getShopId() { return shopId; }
    public UUID getClientId() { return clientId; }
    public UUID getServiceId() { return serviceId; }
    public UUID getWorkerId() { return workerId; }
    public void setWorkerId(UUID workerId) { this.workerId = workerId; }
    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public BigDecimal getExpectedPrice() { return expectedPrice; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
