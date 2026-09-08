/**
 * Core domain building blocks shared by every aggregate:
 * <ul>
 *   <li>{@code Money} - exact monetary arithmetic (BigDecimal-backed, never
 *       floating point), per ADR-011 section 50.</li>
 *   <li>{@code StableId} - a locally-generatable unique identifier (UUID-based)
 *       that a client can create before any server contact, per ADR-010 section 14.
 *       Every synchronizable entity's identity is a StableId.</li>
 *   <li>{@code AggregateRoot} / {@code DomainEvent} - base types for aggregate
 *       roots and the domain events they raise.</li>
 * </ul>
 */
package com.ratibu.common.domain;
