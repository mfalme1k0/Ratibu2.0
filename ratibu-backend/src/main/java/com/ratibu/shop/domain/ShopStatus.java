package com.ratibu.shop.domain;

/**
 * Shop lifecycle (ADR-003 §14). Shops are not physically deleted; they move
 * from ACTIVE to CLOSED so historical bookings, payments, and reports remain
 * attributable to the same StableId.
 */
public enum ShopStatus {
    ACTIVE,
    CLOSED
}
