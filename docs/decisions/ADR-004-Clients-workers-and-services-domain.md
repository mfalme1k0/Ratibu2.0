# ADR-004: Clients, Workers and Services Domain Model

**Status:** Proposed
**Date:** 2026-08-31
**Decision Type:** Product / Architecture
**Project:** Ratibu

---

## 1. Context

Ratibu's core purpose is to help businesses manage their daily operations while turning those operations into useful business insights.

The primary operational participants are:

* Clients
* Workers
* Services
* Shops
* Bookings

ADR-003 established that:

* A Business is the organizational boundary.
* A Shop is the operational and financial boundary.
* Clients belong to the Business.
* Workers belong to the Business and may be assigned to multiple Shops.
* Services are defined at the Business level and may be configured per Shop.

The system now needs to define these concepts more precisely before the booking lifecycle is designed.

The model must support both a small business such as:

```text id="f5tj1x"
Owner
  │
  └── Business
        │
        └── Shop
              ├── Worker
              └── Services
```

and a larger business such as:

```text id="u3i2dv"
Owner
  │
  └── Business
        ├── Shop A
        │     ├── Worker A
        │     └── Worker B
        │
        ├── Shop B
        │     ├── Worker C
        │     └── Worker D
        │
        └── Shop C
              └── Worker E
```

while maintaining a business-wide view of clients and services.

---

# 2. Decision

Ratibu will model **Clients, Workers and Services as business-level concepts**, while allowing workers and services to have shop-specific associations or configurations.

The conceptual structure is:

```text id="4t6e3v"
                         BUSINESS
                            │
             ┌──────────────┼──────────────┐
             │              │              │
          CLIENTS         WORKERS       SERVICES
                            │              │
                     Shop Assignments   Shop Config
                            │              │
                            └──────┬───────┘
                                   ↓
                                BOOKINGS
```

This preserves business-wide information while maintaining shop-level operational context.

---

# 3. Clients

## 3.1 Client Ownership

Clients will belong to the Business rather than to an individual Shop.

For example:

```text id="zzv6c1"
Beauty Empire
      │
      └── Jane
```

Jane may then interact with multiple shops:

```text id="b4q5p6"
Jane
 │
 ├── Westlands booking
 ├── Kilimani booking
 └── Westlands booking
```

Ratibu will therefore maintain one business-wide client identity.

---

# 4. Client Identity

Ratibu should avoid creating duplicate client records merely because a client visits different shops.

For example, the system should not create:

```text id="5s3n1w"
Jane - Westlands
Jane - Kilimani
```

Instead:

```text id="6a3f9e"
Business
   │
   └── Jane
        │
        ├── Westlands
        └── Kilimani
```

This allows Ratibu to calculate a complete client history.

---

# 5. Client Information

The initial client profile should contain information useful to the business without unnecessarily collecting sensitive personal data.

Potential information includes:

* Name
* Phone number
* Email where applicable
* Notes relevant to the business
* Date of first interaction
* Date of most recent interaction
* Client status

Additional information can be introduced later based on actual product requirements.

---

# 6. Client History

Client history will be derived from business activity.

Potential history includes:

* Bookings
* Services received
* Shops visited
* Workers who served the client
* Payments
* Total spending
* Visit frequency
* Last visit

This history will form part of Ratibu's business intelligence capabilities.

For example:

> Jane has visited the business 15 times and spent KSh 42,000 across three shops.

---

# 7. Client Deactivation

Clients should not normally be physically deleted once they have historical business activity.

Instead, a client may be marked inactive.

For example:

```text id="v2c9e0"
Jane
Status: INACTIVE
```

Historical bookings and financial records remain intact.

This follows the same historical-data principle established for Shops.

---

# 8. Workers

Workers belong to the Business.

A Worker represents a person who performs operational work for the business.

For example:

```text id="r1h8za"
Business
   │
   ├── Mary
   ├── John
   └── Brian
```

Workers may then be assigned to one or more shops.

---

# 9. Worker Profile vs User Account

A Worker Profile is independent from a Ratibu User Account.

A worker may therefore exist as:

```text id="7i7y1j"
Worker Profile
```

without:

```text id="1u0r2q"
User Account
```

For example:

```text id="f4c2b8"
Business
│
├── Mary
│    └── User Account ✓
│
├── John
│    └── User Account ✗
│
└── Brian
     └── User Account ✗
```

This allows an owner to track all workers even if only some workers actively use Ratibu.

A worker may later be invited to create or connect a User Account.

---

# 10. Worker-Shop Assignment

Workers may be assigned to one or multiple shops.

For example:

```text id="o7m9p4"
Mary
 ├── Westlands
 └── Kilimani
```

while:

```text id="n4c8s1"
John
 └── CBD
```

This supports:

* Workers permanently assigned to one shop
* Workers working across multiple branches
* Temporary operational arrangements
* Future worker transfers

A worker's shop assignment determines where they may perform operational work.

---

# 11. Worker Assignment History

Worker-shop assignments should preserve historical information.

For example:

```text id="8e7x3k"
Mary
 ├── Westlands
 │     Jan → June
 │
 └── Kilimani
       July → present
```

If Mary moves from Westlands to Kilimani, historical bookings should remain associated with the shop where they occurred.

The system should not rewrite historical activity simply because a worker's current assignment changed.

---

# 12. Worker Availability

Worker availability will be considered part of the booking system.

The model should eventually support information such as:

* Working days
* Working hours
* Shop assignment
* Availability
* Time off
* Unavailable periods

However, detailed scheduling rules will be defined in the Booking Lifecycle ADR rather than this ADR.

The important principle is:

> A worker should only be available for booking where the worker is assigned to the relevant shop and is available at the requested time.

---

# 13. Worker Services

Not every worker necessarily performs every service offered by the business.

For example:

```text id="u3ftl6"
Services
├── Haircut
├── Coloring
├── Braiding
└── Manicure
```

Workers may have different capabilities:

```text id="l5xq2m"
Mary
├── Haircut
├── Coloring
└── Braiding

John
├── Haircut
└── Manicure
```

Ratibu should therefore support a relationship between workers and services.

This allows the booking system to avoid assigning a worker to a service they do not perform.

---

# 14. Services

Services will be defined at the Business level.

For example:

```text id="8v9gsa"
Beauty Empire
│
├── Haircut
├── Hair Coloring
├── Braiding
└── Manicure
```

A service represents something the business offers to clients.

---

# 15. Shop-Specific Service Configuration

Although services are defined by the Business, individual Shops may configure them.

For example:

```text id="y8yrm3"
Business
│
└── Haircut
      │
      ├── Westlands → Available, KSh 500
      ├── Kilimani  → Available, KSh 550
      └── CBD       → Unavailable
```

This avoids forcing the owner to create duplicate services for every shop.

---

# 16. Service Pricing

Service prices may differ between shops.

For example:

```text id="w4f7zp"
Haircut

Westlands     KSh 500
Kilimani      KSh 550
CBD           KSh 450
```

The price used by a booking should be determined from the service configuration applicable to the relevant shop.

However, historical bookings must retain the price that was applicable when the booking/payment occurred.

Changing the current service price must not rewrite historical financial information.

---

# 17. Historical Service Information

Suppose:

```text id="0x5b0a"
Haircut
Price: KSh 500
```

A month later, the owner changes it to:

```text id="h7k6vp"
Haircut
Price: KSh 600
```

Existing bookings should continue to reflect:

```text id="4shxw8"
Original booking price: KSh 500
```

rather than automatically becoming KSh 600.

This is necessary for:

* Financial accuracy
* Historical reporting
* Cash-book accuracy
* Revenue analysis
* Client spending history

---

# 18. Service Availability

A service may be:

* Available at all shops
* Available at selected shops
* Temporarily unavailable
* Permanently discontinued

For example:

```text id="a3m5p8"
Hair Coloring
│
├── Westlands ✓
├── Kilimani  ✓
└── CBD       ✗
```

The exact lifecycle rules will be finalized during implementation.

---

# 19. Service Lifecycle

Services with historical transactions should not be physically deleted.

Instead, a service may be deactivated or discontinued.

For example:

```text id="b6e8r1"
Hair Coloring
Status: DISCONTINUED
```

Historical bookings and financial records remain intact.

This follows the same principle used for Shops and Clients.

---

# 20. Relationships

The resulting relationships are:

```text id="i6v9f2"
BUSINESS
   │
   ├── CLIENTS
   │
   ├── WORKERS
   │      │
   │      ├── Shop A
   │      └── Shop B
   │
   ├── SERVICES
   │      │
   │      ├── Shop A configuration
   │      ├── Shop B configuration
   │      └── Shop C configuration
   │
   └── SHOPS
          │
          └── BOOKINGS
```

A booking will eventually connect:

```text id="g7d4w1"
Client
   +
Shop
   +
Worker
   +
Service
   ↓
Booking
```

This will be formally defined in ADR-005.

---

# 21. Business Intelligence Implications

The domain model is intentionally designed to support Ratibu's insight system.

Because clients are business-wide, Ratibu can calculate:

> Client spending across all shops.

Because workers have shop assignments, Ratibu can calculate:

> Worker performance by shop and across the business.

Because services are business-wide with shop configuration, Ratibu can calculate:

> Which services perform best across the business and at individual shops.

Because historical prices are preserved, Ratibu can calculate:

> Revenue trends without historical data being distorted by later price changes.

---

# 22. Core Domain Rules

The following rules are established:

### Rule 1

> Clients belong to the Business.

### Rule 2

> A client may interact with multiple Shops within the same Business.

### Rule 3

> Workers belong to the Business.

### Rule 4

> A Worker Profile does not require a User Account.

### Rule 5

> Workers may be assigned to one or multiple Shops.

### Rule 6

> Worker-shop assignments determine where a worker may perform operational work.

### Rule 7

> Workers may be associated with the Services they are capable of performing.

### Rule 8

> Services are defined at the Business level.

### Rule 9

> Services may have shop-specific availability and configuration.

### Rule 10

> Service prices may differ between Shops.

### Rule 11

> Historical transactions must retain the relevant historical service price and context.

### Rule 12

> Clients, Workers and Services with historical activity should be deactivated rather than physically deleted.

### Rule 13

> Changes to current client, worker, shop or service configuration must not rewrite historical transactions.

---

# 23. Resulting Conceptual Model

```text id="q3c9am"
                              BUSINESS
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
           CLIENTS            WORKERS           SERVICES
              │                  │                  │
              │            Shop Assignments     Shop Config
              │                  │                  │
              │            Service Skills       Pricing
              │                  │              Availability
              │                  │                  │
              └──────────────────┼──────────────────┘
                                 │
                              BOOKING
                                 │
                    ┌────────────┼────────────┐
                    │            │            │
                  CLIENT       WORKER       SERVICE
                                 │
                                SHOP
```

---

# 24. Consequences

## Positive consequences

* Maintains a single client identity across shops.
* Enables complete client history.
* Allows workers to work at multiple shops.
* Allows workers to exist without requiring application accounts.
* Prevents workers from being assigned services they do not perform.
* Allows businesses to define services once and configure them per shop.
* Supports different prices between shops.
* Preserves historical financial accuracy.
* Supports detailed business insights.
* Provides a strong foundation for bookings.

## Negative consequences

* Worker-shop assignments introduce additional domain complexity.
* Worker-service capabilities introduce another relationship to maintain.
* Shop-specific service pricing requires additional configuration.
* Historical values must be preserved separately from current configuration.
* Client and worker lifecycle management becomes more complex.

These costs are accepted because they directly support Ratibu's multi-shop and business-intelligence goals.

---

# 25. Next Decision

The next ADR will define the **Booking Lifecycle**.

It will determine:

* What constitutes a booking
* Booking creation
* Booking rescheduling
* Booking confirmation
* Booking completion
* Cancellation
* No-shows
* Unpaid bookings
* Partial payments
* Completed services
* Worker availability
* Client relationships
* Service pricing at booking time
* Shop association
* What events generate financial activity
* How bookings interact with the Cash Book

The booking lifecycle will become the bridge between Ratibu's operational domain and its financial domain.
