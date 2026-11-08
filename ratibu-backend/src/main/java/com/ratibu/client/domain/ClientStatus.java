package com.ratibu.client.domain;

/**
 * Lifecycle status of a Client (ADR-004 §7). Historical activity is preserved
 * when INACTIVE; clients are not physically deleted once they have history.
 */
public enum ClientStatus {
    ACTIVE,
    INACTIVE
}
