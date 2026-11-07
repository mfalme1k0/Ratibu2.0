package com.ratibu.worker.domain;

/**
 * Lifecycle status of a Worker Profile (ADR-004 §8, mirrors client deactivation
 * principle in ADR-004 §7). Historical activity is preserved when INACTIVE.
 */
public enum WorkerStatus {
    ACTIVE,
    INACTIVE
}
