/**
 * Shared kernel used across all aggregates: value objects (Money, StableId),
 * base classes (AggregateRoot, DomainEvent), and shared exception types.
 * Nothing in this package or its subpackages may depend on any single
 * aggregate package - dependencies only flow the other way.
 */
package com.ratibu.common;
