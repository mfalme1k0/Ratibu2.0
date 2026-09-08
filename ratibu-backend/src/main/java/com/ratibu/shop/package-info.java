/**
 * Shop aggregate: the operational and financial boundary within a Business. Every operational and financial event ultimately references a Shop or, per ADR-013 section 2, the Business directly. See ADR-003 sections 5 to 19.
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
package com.ratibu.shop;
