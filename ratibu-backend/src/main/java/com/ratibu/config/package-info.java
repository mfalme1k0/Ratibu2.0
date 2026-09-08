/**
 * Spring configuration classes: security filter chain, JPA/Flyway setup,
 * web/CORS configuration, and the multi-tenancy query-scoping mechanism
 * (every data access path scoped by Business Membership, per ADR-014
 * section 4.4 - enforced here, not re-implemented per endpoint).
 */
package com.ratibu.config;
