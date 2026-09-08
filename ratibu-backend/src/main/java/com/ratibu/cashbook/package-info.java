/**
 * Cash Book and EOD Reconciliation aggregate: physical cash tracking and end-of-day reconciliation, including the EOD supersede-and-flag conflict rule. See ADR-006, ADR-011, and ADR-013 section 1.
 *
 * <p>Layout convention (applies to every aggregate package in this project):
 * <ul>
 *   <li>{@code domain} - the aggregate root, value objects, domain events, and the
 *       repository <em>interface</em>. Business rules and invariants live here. Per
 *       ADR-012 section 18, this code must not depend on Spring, JPA, or the web layer.</li>
 *   <li>{@code application} - use-case/application services that orchestrate a single
 *       use case, including coordination with other aggregates via their IDs and
 *       application services (never by reaching into another aggregate's internals -
 *       ADR-012 section 19).</li>
 *   <li>{@code infrastructure} - JPA entities, Spring Data repositories, and mappers
 *       implementing the domain repository interface.</li>
 *   <li>{@code api} - REST controllers and DTOs for this aggregate. Controllers are
 *       thin: capture input, invoke an application service, return a result
 *       (ADR-012 section 18).</li>
 * </ul>
 */
package com.ratibu.cashbook;
