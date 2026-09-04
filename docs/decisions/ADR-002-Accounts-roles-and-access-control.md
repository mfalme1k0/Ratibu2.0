# ADR-002: Accounts, Business Memberships, Roles and Access Control

**Status:** Proposed
**Date:** 2026-08-31
**Decision Type:** Product / Architecture
**Project:** Ratibu

---

## 1. Context

Ratibu is intended to support businesses ranging from a single owner operating one shop to businesses with multiple shops and up to approximately 50 workers.

A simple owner-to-business relationship is insufficient for the long-term product vision because users may have different responsibilities within a business and may need access to different shops.

For example:

```text
Owner
  │
  └── Business
        ├── Shop A
        ├── Shop B
        └── Shop C
```

A worker may only operate at Shop A, while a manager may oversee Shops A and B. The owner should have visibility across the entire business.

At the same time, not every worker necessarily needs a Ratibu login. A small business owner may want to record a worker's activity without requiring that worker to become an application user.

The system therefore needs to distinguish between:

* A person's Ratibu account
* Their membership in a business
* Their role within that business
* The shops they can access
* Their worker profile

---

# 2. Problem

Ratibu needs an identity and authorization model that can support:

* Single-owner businesses
* Multi-worker businesses
* Multi-shop businesses
* Users belonging to multiple businesses
* Different roles within different businesses
* Workers who do not have Ratibu accounts
* Workers assigned to multiple shops
* Shop-scoped access
* Business-wide access
* Future expansion of permissions

The model must remain simple enough for small businesses while providing a foundation for larger businesses.

---

# 3. Decision

Ratibu will use an **account-based identity model with business memberships**.

A user's access to a business will be represented through a **Business Membership**.

The conceptual relationship is:

```text
User Account
      │
      └── Business Membership
              │
              ├── Business
              ├── Role
              └── Shop Assignment(s)
```

A user may belong to one or more businesses, and their role and shop access may differ for each business.

---

# 4. User Account

A **User Account** represents a person who can authenticate and interact with Ratibu.

The account represents identity rather than a business role.

For example:

```text
Jane
  │
  └── User Account
```

Jane's role is determined by her membership in a particular business.

This separation allows the same account to participate in different businesses in the future.

---

# 5. Business Membership

A **Business Membership** represents a user's relationship with a specific business.

Conceptually:

```text
User
 │
 ├── Business Membership
 │       Business: Beauty Empire
 │       Role: OWNER
 │
 └── Business Membership
         Business: Mary's Cleaning Services
         Role: WORKER
```

The membership will determine:

* Which business the user belongs to
* Their role within the business
* Which shops they can access
* Which permissions they receive

This prevents the user's global account from being tightly coupled to one business.

---

# 6. Roles

The initial Ratibu roles will be:

1. **Owner**
2. **Manager**
3. **Worker**

Additional roles may be introduced later if actual product requirements justify them.

---

## 6.1 Owner

The Owner has full access to the business.

The owner can generally:

* Manage the business
* Manage shops
* Manage workers
* Manage clients
* Manage services
* Manage bookings
* View financial information
* Manage the cash book
* Perform end-of-day reconciliation
* View business-wide reports
* View business insights
* Manage user access and permissions

The owner represents the highest level of authority within the business.

---

## 6.2 Manager

A Manager is responsible for managing one or more assigned shops.

A manager may:

* Manage bookings
* Manage workers within assigned shops
* Manage clients
* Manage services where permitted
* View shop-level performance
* Manage relevant cash-book activity where permitted
* Perform end-of-day operations where permitted
* View relevant reports

A manager should not automatically receive access to the entire business.

Their access will be constrained by their assigned shops and permissions.

---

## 6.3 Worker

A Worker primarily performs the operational work of the business.

A worker may:

* View assigned bookings
* Create or modify bookings where permitted
* View relevant clients
* View services
* Record service completion
* View their own activity
* Perform other operational tasks permitted by the business

Workers should not automatically have access to:

* Business-wide financial information
* Other shops' financial information
* Owner-level insights
* Business-wide reports
* Other workers' private performance information

Financial permissions may be granted where the business requires them.

---

# 7. Worker Profiles vs User Accounts

Ratibu will distinguish between a **Worker Profile** and a **User Account**.

A worker may exist in the business without having a Ratibu account.

For example:

```text
Business
   │
   ├── Mary — Worker Profile + User Account
   ├── John — Worker Profile
   └── Brian — Worker Profile
```

John and Brian can be recorded as workers and associated with bookings without necessarily being able to log into Ratibu.

The owner may later invite them to Ratibu.

```text
Worker Profile
      │
      ↓
Invite Worker
      │
      ↓
User Account
      │
      ↓
Business Membership
```

This allows small businesses to use Ratibu without requiring every worker to immediately adopt the application.

---

# 8. Shop Assignments

Access to shops will be explicit.

A business may contain:

```text
Business
   │
   ├── Shop A
   ├── Shop B
   └── Shop C
```

A worker may be assigned to:

```text
Worker A
   └── Shop A
```

or:

```text
Worker B
   ├── Shop A
   └── Shop B
```

A manager may similarly manage one or multiple shops.

The system should not assume that a worker permanently belongs to exactly one shop.

---

# 9. Access Control Model

Ratibu will use the following conceptual authorization model:

```text
Access =
    Business Membership
    +
    Role
    +
    Shop Assignment
    +
    Permission
```

Before allowing an operation, Ratibu should consider:

1. Is the user a member of the business?
2. What role does the user have?
3. Does the user have access to the relevant shop?
4. Does their role/permission allow the requested operation?

This allows the system to distinguish between business-wide and shop-specific operations.

---

# 10. Example: Owner Access

Consider:

```text
Jane
  │
  └── Beauty Empire
        ├── Westlands
        ├── Kilimani
        └── CBD
```

Jane is the Owner.

She can view:

```text
All Shops
   │
   ├── Westlands
   ├── Kilimani
   └── CBD
```

and consolidated information such as:

* Total revenue
* Total expenses
* Total bookings
* Business cash position
* Worker performance
* Shop performance
* Business-wide insights

---

# 11. Example: Manager Access

Suppose Mary manages:

```text
Beauty Empire
   ├── Westlands ← Mary
   ├── Kilimani  ← Mary
   └── CBD
```

Mary can access Westlands and Kilimani.

She should not automatically be able to access CBD.

Her information scope becomes:

```text
Mary
 │
 ├── Westlands
 │
 └── Kilimani
```

The owner retains access to all three shops.

---

# 12. Example: Worker Access

Suppose Brian works at Westlands.

```text
Brian
 │
 └── Beauty Empire
       │
       └── Westlands
```

Brian can interact with relevant operational data for Westlands.

He should not automatically see:

```text
Kilimani revenue
CBD expenses
Business-wide profit
Owner-level insights
```

unless explicitly permitted.

---

# 13. Multiple Business Memberships

The account model will allow a user to belong to multiple businesses.

For example:

```text
Jane
 │
 ├── Beauty Empire
 │      Role: OWNER
 │
 └── Jane's Cleaning Services
        Role: OWNER
```

Or:

```text
Mary
 │
 ├── Beauty Empire
 │      Role: WORKER
 │
 └── Another Business
        Role: MANAGER
```

The MVP may initially present a simple single-business experience, but the underlying model should not prevent multiple business memberships.

---

# 14. Multiple Owners

The underlying membership model should allow more than one user to hold an ownership-level role.

Conceptually:

```text
Business
   │
   ├── Jane — OWNER
   └── John — OWNER
```

However, the initial MVP may restrict a business to a single owner if this simplifies onboarding and management.

The data model should not make multiple ownership fundamentally impossible.

---

# 15. Financial Access

Financial access will be controlled separately from general operational access.

A user being able to manage bookings does not automatically mean they can:

* View the cash book
* Record expenses
* Perform end-of-day reconciliation
* View business revenue
* View profit-related information

For example:

```text
Worker
  │
  ├── Bookings       ✓
  ├── Clients        ✓
  ├── Services       ✓
  ├── Cash Book      ✗
  └── Business Data  ✗
```

A business may later grant additional permissions where necessary.

This keeps Ratibu flexible without requiring a large number of roles.

---

# 16. Why Cashier Is Not an Initial Role

A dedicated **Cashier** role will not be part of the initial role set.

Instead, financial responsibilities will initially be treated as permissions that can be assigned to appropriate roles.

For example:

```text
Manager
   ├── View Cash Book       ✓
   ├── Record Cash          ✓
   └── Close Day            ✓
```

while:

```text
Worker
   ├── View Cash Book       ✗
   ├── Record Cash          ✗
   └── Close Day            ✗
```

If real-world requirements demonstrate that a dedicated cashier role is necessary, it can be introduced later.

---

# 17. Design Principle: Simple by Default

Ratibu should not expose the full complexity of the authorization model to small businesses.

A single-shop owner should experience something close to:

```text
Owner
  │
  └── My Shop
```

without needing to understand:

* Business memberships
* Shop scopes
* Permission matrices
* Organizational hierarchies

The underlying architecture can support these capabilities while the UI remains simple.

As a business grows, additional capabilities become available.

---

# 18. Resulting Conceptual Model

The resulting model is:

```text
                         USER ACCOUNT
                              │
                    BUSINESS MEMBERSHIP
                              │
              ┌───────────────┼───────────────┐
              │               │               │
           BUSINESS          ROLE        SHOP ACCESS
              │
       ┌──────┼───────────────┐
       │      │               │
     SHOPS  CLIENTS        SERVICES
       │
    WORKERS
       │
    BOOKINGS
```

With roles:

```text
OWNER
  ↓
Full business access

MANAGER
  ↓
Assigned-shop management

WORKER
  ↓
Operational access
```

And worker identity separated from authentication:

```text
Worker Profile
      │
      └── optional User Account
```

---

# 19. Consequences

### Positive consequences

* Supports both single-shop and multi-shop businesses.
* Allows businesses to grow without changing their fundamental account structure.
* A worker can exist without requiring an application account.
* Workers can eventually be invited to use Ratibu.
* Users can belong to multiple businesses.
* Roles can differ between businesses.
* Workers and managers can be assigned to multiple shops.
* Financial access can be separated from general operational access.
* The authorization model can grow without creating excessive numbers of roles.
* Small businesses can maintain a simple user experience.

### Negative consequences

* Business memberships introduce additional domain complexity.
* Authorization requires considering both role and shop scope.
* Worker profiles and user accounts must be treated as separate concepts.
* Multi-business membership adds complexity that may not be immediately visible to MVP users.
* Fine-grained permissions may become complex as Ratibu grows.

These costs are accepted because the model provides a strong foundation for Ratibu's intended growth from small single-shop businesses to larger multi-shop businesses.

---

# 20. Decision Summary

Ratibu will use:

```text
User Account
      ↓
Business Membership
      ↓
Role + Shop Assignment
```

with three initial roles:

```text
OWNER
MANAGER
WORKER
```

Workers may exist as profiles without Ratibu accounts.

Access will be determined by:

```text
Business Membership
        +
Role
        +
Shop Assignment
        +
Permission
```

A dedicated cashier role will not be required initially.

The architecture will support multiple business memberships and multiple owners conceptually, even if the MVP imposes simpler operational restrictions.

---

## 21. Next Decision

The next decision will define the **Business and Shop domain structure**.

It will answer questions such as:

* What exactly is a Business?
* What exactly is a Shop/Branch?
* Can a business have different types?
* Can shops have different services?
* Are prices global or shop-specific?
* Can a shop operate independently?
* Can a business transfer workers between shops?
* How are business-wide and shop-specific clients handled?
* What information belongs at Business level versus Shop level?

These decisions will form the foundation for Ratibu's operational and financial domain model.
