# ADR-003: Business and Shop Organizational Structure

**Status:** Proposed
**Date:** 2026-08-31
**Decision Type:** Product / Architecture
**Project:** Ratibu

---

## 1. Context

Ratibu is intended to support businesses ranging from a single owner operating one shop to larger businesses with multiple shops and potentially up to approximately 50 workers.

The original Ratibu concept primarily focused on individual businesses. The redesigned system must additionally support owners who operate multiple shops under the same business.

For example:

```text
Owner
  │
  └── Business
        ├── Shop A
        ├── Shop B
        └── Shop C
```

The system must allow the owner to manage each shop independently while also providing a consolidated view of the entire business.

This requires a clear distinction between:

* The **Business**, representing the organizational/ownership boundary
* The **Shop**, representing the operational and financial boundary

Without this distinction, multi-shop reporting, cash management, worker assignments, and business-wide insights would become difficult to implement consistently.

---

# 2. Problem

Ratibu needs to support both:

### Single-shop businesses

```text
Business
   │
   └── Shop
```

and:

### Multi-shop businesses

```text
Business
   │
   ├── Shop A
   ├── Shop B
   └── Shop C
```

The system must be able to:

* Maintain independent shop operations
* Maintain shop-level financial records
* Aggregate information across shops
* Track workers across multiple shops
* Maintain clients across the entire business
* Share services across shops
* Allow shop-specific configurations
* Preserve historical information when a shop closes
* Provide both shop-level and business-level insights

---

# 3. Decision

Ratibu will use a **Business → Shop organizational model**.

The Business represents the organizational and ownership boundary.

The Shop represents the operational and financial boundary.

```text
Owner
  │
  └── Business
        │
        ├── Shop A
        ├── Shop B
        └── Shop C
```

A single-shop business is simply a Business containing one Shop.

There will not be a separate architecture or product mode for single-shop businesses.

---

# 4. Business

A **Business** represents the organization managed through Ratibu.

Business-level information may include:

* Business identity
* Business profile
* Business members
* Business settings
* Business-wide clients
* Business-wide services
* Consolidated financial information
* Consolidated operational information
* Business-wide reports
* Business-wide insights

The Business is the primary organizational boundary.

---

# 5. Shop

A **Shop** represents a physical or operational branch of a Business.

For example:

```text
Beauty Empire
│
├── Westlands
├── Kilimani
└── CBD
```

Each Shop maintains its own operational and financial activity.

Shop-level information may include:

* Shop identity
* Workers assigned to the shop
* Bookings
* Payments
* Expenses
* Cash book
* End-of-day records
* Shop-level reports
* Shop-level insights

Every Shop belongs to exactly one Business.

---

# 6. Single-Shop Businesses

A Business must contain at least one Shop for normal operation.

The smallest valid Ratibu business is therefore:

```text
Business
   │
   └── Shop
```

A business does not need multiple shops to use Ratibu.

This ensures that single-shop businesses and multi-shop businesses use the same fundamental model.

---

# 7. Multi-Shop Businesses

A Business may contain multiple Shops.

For example:

```text
Business
   │
   ├── Westlands
   ├── Kilimani
   ├── CBD
   └── Rongai
```

Each shop operates independently while remaining part of the same business.

The owner can therefore view:

### Individual shop

```text
Westlands
Revenue: KSh 124,000
Bookings: 86
Expenses: KSh 32,000
```

### Entire business

```text
Business
Revenue: KSh 284,500
Bookings: 186
Expenses: KSh 96,200
```

Business-level information will be derived from the underlying shop-level activity.

---

# 8. Operational Boundary

Operational events will be associated with a Shop.

Examples include:

```text
Booking       → Shop
Service       → Shop context
Worker activity → Shop
Payment       → Shop
```

This ensures that Ratibu can determine where an operational event occurred.

A booking should therefore conceptually follow:

```text
Booking
   ↓
Shop
   ↓
Business
```

rather than being associated only with the Business.

---

# 9. Financial Boundary

Financial events will also be associated with a Shop.

Examples include:

```text
Payment
Expense
Cash-in
Cash-out
Offline income
EOD reconciliation
```

Each belongs to a specific Shop.

This allows each branch to maintain an independent cash position.

```text
Westlands
   └── Cash Book

Kilimani
   └── Cash Book

CBD
   └── Cash Book
```

The Business can then obtain a consolidated financial view by aggregating shop-level records.

---

# 10. No Duplicate Business-Level Financial Transactions

Ratibu will not create duplicate financial transactions solely to represent consolidated business totals.

For example:

```text
Westlands    KSh 124,000
Kilimani      KSh 96,500
CBD           KSh 64,000
```

The business revenue:

```text
KSh 284,500
```

will be calculated from the underlying shop-level transactions.

Conceptually:

```text
Shop Financial Events
        ↓
Aggregation
        ↓
Business Financial View
```

This avoids maintaining multiple sources of truth.

---

# 11. Clients

Clients will belong to the Business rather than being permanently tied to a single Shop.

A client may interact with multiple Shops.

For example:

```text
Business
   │
   └── Jane
        │
        ├── Westlands booking
        ├── Kilimani booking
        └── Westlands booking
```

This allows Ratibu to maintain a complete business-wide client history.

The owner can therefore see information such as:

* Total visits
* Total spending
* Most visited shop
* Most purchased services
* Last visit
* Client retention

---

# 12. Workers

Workers will belong to the Business and may be assigned to one or multiple Shops.

For example:

```text
Business
   │
   ├── Mary
   │      ├── Westlands
   │      └── Kilimani
   │
   ├── John
   │      └── CBD
   │
   └── Brian
          └── Westlands
```

The system must therefore not assume that a worker permanently belongs to exactly one shop.

Shop assignments will determine where the worker can operate.

---

# 13. Services

Services will primarily be defined at the Business level.

For example:

```text
Business Services

Haircut
Beard Trim
Hair + Beard
Hair Coloring
```

Shops may then configure those services.

For example:

```text
Westlands
Haircut → KSh 500

Kilimani
Haircut → KSh 550
```

This prevents the owner from having to create duplicate services for every branch while still allowing shop-specific differences.

The precise pricing and availability rules will be defined in a later ADR.

---

# 14. Shop Lifecycle

Shops containing historical activity should not be physically deleted.

Instead, a Shop may become inactive or closed.

Conceptually:

```text
ACTIVE
   ↓
CLOSED
```

Historical records associated with the shop must remain available.

For example:

```text
Westlands Branch
Status: CLOSED

Historical Revenue:
KSh 4,200,000
```

Closing a shop must not remove or corrupt historical business information.

The exact lifecycle states will be defined during implementation design.

---

# 15. Stable Shop Identity

A Shop's identity must not depend on its name.

For example:

```text
"Westlands Branch"
```

may later become:

```text
"Westlands Premium"
```

while remaining the same Shop.

Historical bookings, payments, expenses, and reports must continue to reference the same Shop.

---

# 16. Shop Addition

Adding a new Shop should not require creating another Business or another owner account.

For example:

Initial state:

```text
Business
└── Westlands
```

After adding a shop:

```text
Business
├── Westlands
└── Kilimani
```

The new shop immediately becomes part of the same business and contributes to future consolidated reporting.

---

# 17. Business and Shop Insights

Ratibu will support insights at both levels.

### Shop-level

> Westlands revenue increased 18% this month.

### Business-level

> Total business revenue increased 14% this month.

The business-level insight can be derived from shop-level information.

For example:

```text
Shop Performance
      ↓
Aggregation
      ↓
Business Performance
      ↓
Business Insight
```

This allows Ratibu to provide both operational detail and an overall business perspective.

---

# 18. Core Structural Rules

The following rules are established:

### Rule 1

> A Business is the organizational and ownership boundary.

### Rule 2

> A Shop is the operational and financial boundary.

### Rule 3

> Every Shop belongs to exactly one Business.

### Rule 4

> Every operational and financial event must be associated with a Shop.

### Rule 5

> Clients belong to the Business and may interact with multiple Shops.

### Rule 6

> Workers belong to the Business and may be assigned to one or multiple Shops.

### Rule 7

> Services are defined at the Business level by default and may be configured per Shop.

### Rule 8

> Business-level financial and operational metrics are derived from Shop-level data.

### Rule 9

> Historical Shops are not physically deleted after they contain business activity.

### Rule 10

> A single-shop business is a Business containing one Shop.

---

# 19. Resulting Domain Structure

The resulting conceptual structure is:

```text
                         BUSINESS
                            │
          ┌─────────────────┼─────────────────┐
          │                 │                 │
       CLIENTS           WORKERS          SERVICES
          │                 │                 │
          │          ┌──────┴──────┐           │
          │          │             │           │
          │        Shop A        Shop B        │
          │          │             │           │
          │       Bookings      Bookings       │
          │       Payments      Payments       │
          │       Expenses      Expenses       │
          │       Cash Book     Cash Book      │
          │          │             │           │
          └──────────┴─────────────┴───────────┘
                            │
                     BUSINESS INSIGHTS
```

---

# 20. Consequences

## Positive consequences

* Supports both single-shop and multi-shop businesses.
* Allows an owner to add shops without creating new accounts or businesses.
* Provides clear financial separation between shops.
* Enables consolidated business reporting.
* Enables branch-to-branch comparisons.
* Allows workers to operate across multiple shops.
* Maintains a complete client history across the business.
* Avoids duplicate business-level financial records.
* Supports shop-specific configuration while maintaining shared business definitions.
* Preserves historical data when shops close.
* Provides a strong foundation for business intelligence.

## Negative consequences

* The domain model is more complex than a single-shop booking system.
* Shop-level financial isolation requires careful transaction modeling.
* Multi-shop workers introduce additional assignment logic.
* Shared services with shop-specific configuration introduce additional rules.
* Consolidated reporting requires aggregation across shops.
* Shop lifecycle management adds complexity.

These costs are accepted because multi-shop support is an explicit part of Ratibu's product vision.

---

# 21. Next Decision

The next ADR will define the **Client, Worker and Service domain model**.

It will establish:

* Client information
* Client lifecycle
* Worker profiles
* Worker-shop assignments
* Worker availability
* Service definitions
* Service pricing
* Shop-specific service configuration
* Whether workers can perform every service
* Client history
* Relationships between clients, workers, services and shops

These decisions will provide the foundation for the Booking Lifecycle ADR that follows.
