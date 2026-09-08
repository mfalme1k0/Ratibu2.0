/**
 * The synchronization engine implementing the offline-first contract defined
 * in ADR-010 and ADR-014 section 7: idempotent operation submission keyed by
 * client-generated StableIds, per-aggregate conflict resolution dispatch, and
 * sync-status tracking. This package does not itself decide domain-specific
 * conflict rules (e.g. the EOD supersede-and-flag rule lives in the cashbook
 * aggregate) - it provides the generic envelope and dispatch mechanism that
 * every aggregate's sync handling plugs into.
 */
package com.ratibu.sync;
