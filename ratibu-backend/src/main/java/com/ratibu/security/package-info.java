/**
 * Authentication (JWT issuance and validation, resolving the authenticated
 * User Account) and authorization (enforcing the permission catalog and
 * scoping rules from ADR-008 - Role + Business Membership + Shop Assignment
 * + Permission = Effective Access). Authorization checks here are the
 * server-side enforcement; client-side permission checks are UX convenience
 * only, per the same principle ADR-010 section 25 applies to data validation.
 */
package com.ratibu.security;
