package com.ratibu.catalog.domain;

/**
 * Lifecycle of a business-level service (ADR-004 §19).
 * DISCONTINUED preserves historical bookings; the service is not deleted.
 */
public enum ServiceStatus {
    ACTIVE,
    DISCONTINUED
}
