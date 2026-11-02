package com.ratibu.business.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA mapping for the {@code business} table. Kept in infrastructure so the
 * domain {@link com.ratibu.business.domain.Business} stays free of persistence
 * annotations (ADR-012 §18).
 */
@Entity
@Table(name = "business")
public class BusinessJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BusinessJpaEntity() {
        // JPA
    }

    public BusinessJpaEntity(UUID id, String name, String currency, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
