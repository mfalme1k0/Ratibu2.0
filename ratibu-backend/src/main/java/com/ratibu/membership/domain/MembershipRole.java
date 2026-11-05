package com.ratibu.membership.domain;

/**
 * Initial Ratibu roles within a Business (ADR-002 §6).
 * Permissions expand via ADR-008 catalog later; role is the coarse grant.
 */
public enum MembershipRole {
    OWNER,
    MANAGER,
    WORKER
}
