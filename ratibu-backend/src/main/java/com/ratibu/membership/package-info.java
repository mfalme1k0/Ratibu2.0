/**
 * Business Membership: links a User Account to a Business with a Role and Shop Assignment(s). This is the sole mechanism by which a user gains access to Business data. See ADR-002 sections 5 to 18 and the permission catalog in ADR-008.
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
package com.ratibu.membership;
