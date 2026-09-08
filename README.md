# Ratibu 2.0

> **Offline-first business management and financial tracking for small businesses.**

Ratibu is a business management platform designed for small businesses that need a simple way to manage their operations, customers, services, workers, bookings, payments, expenses, cash, and business performance.

Ratibu 2.0 is a deliberate redesign of the original concept, with the architecture being defined **before feature implementation**.

The project is currently transitioning from **architecture and domain design into implementation**.

---

## Project Status

**Current stage: Architecture & Foundation**

The first phase of Ratibu 2.0 has focused on understanding and documenting the business domain before writing substantial application code.

Completed so far:

* Domain model defined
* Aggregate boundaries defined
* Architectural Decision Records (ADRs) established
* Offline-first strategy defined
* Authentication and authorization model defined
* Financial and cash-management rules defined
* Maven/Spring Boot backend created
* Backend package structure established
* Shared domain primitives established
* Application entry point established

The next phase is to implement the aggregates and their use cases incrementally, using the ADRs as the architectural reference.

---

# Why Ratibu?

Small businesses often manage their operations using a mixture of:

* Paper notebooks
* Appointment books
* WhatsApp
* Spreadsheets
* Separate cash books
* Personal records

This makes it difficult to get a reliable picture of the business.

A business owner should be able to answer questions such as:

* How much money did the business receive today?
* How much physical cash should be available?
* Which services generate the most revenue?
* Which workers are providing which services?
* How many bookings were completed or cancelled?
* Which payments are still outstanding?
* How is each shop performing?
* What happened while the business was offline?

Ratibu aims to bring these concerns into one system without assuming that the business always has a reliable internet connection.

---

# Architectural Approach

Ratibu 2.0 is being designed around a few fundamental principles.

## Architecture First

The project intentionally started with architectural decisions rather than controllers and database tables.

The ADRs define:

* What the major business concepts are
* Where business rules belong
* Which concepts should be aggregates
* How aggregates interact
* How users gain access to businesses
* How multiple shops are represented
* How offline operations are synchronized
* How financial events are modeled
* How conflicts are resolved

The implementation is expected to follow these decisions rather than allowing the database or framework to dictate the domain model.

---

# Domain Model

Ratibu is organized around business concepts rather than generic CRUD resources.

The current aggregate model includes:

```text
Business
Shop
User Account
Business Membership
Worker
Client
Catalog / Service
Booking
Payment
Expense
Financial Event
Cash Book
Insight
```

Each aggregate owns its own state, rules, and invariants.

Aggregates do not directly reach into each other's internal state.

Instead, coordination occurs through stable identities, application services, and domain events.

---

# Business and Shop

A **Business** represents the organizational boundary.

A **Shop** represents an operational and financial boundary within a business.

Ratibu therefore supports both simple and multi-shop businesses.

### Single shop

```text
Business
└── Shop
```

### Multiple shops

```text
Business
├── Shop A
├── Shop B
└── Shop C
```

This allows a business owner to start with one shop without introducing unnecessary complexity while still supporting businesses that grow into multiple locations.

---

# Identity and Membership

Ratibu separates authentication identity from business participation.

A:

**User Account**

represents a person's global identity.

A:

**Business Membership**

represents that person's relationship with a particular business.

Conceptually:

```text
User Account
     │
     ▼
Business Membership
     │
     ├── Business
     ├── Role
     └── Shop Assignment(s)
```

A user therefore does not automatically gain access to every business in the system.

Access is established through membership and authorization rules.

---

# Worker Model

A **Worker Profile** represents a worker from the perspective of a business.

A worker does not necessarily need a Ratibu login.

Therefore:

```text
Worker Profile
       │
       ├── Can exist without User Account
       │
       └── May be associated with a Business Membership
```

This distinction allows a business to keep operational records for workers who do not use the application themselves.

Worker-service capabilities are also treated as part of the domain rather than being left entirely to the UI.

---

# Clients

Clients represent the business's customer identity.

A client is **business-wide**, rather than being duplicated for every shop.

For example:

```text
Business
│
├── Shop A
│
├── Shop B
│
└── Client
```

The same client can therefore interact with different shops belonging to the same business without creating separate customer identities.

Duplicate detection and client identity rules are part of the domain model.

---

# Catalog and Services

Services are defined at the business level while allowing individual shops to determine:

* Availability
* Pricing
* Service-specific configuration

Ratibu also preserves historical pricing information where necessary.

This means changing the current service price should not rewrite historical transactions.

---

# Bookings

A Booking represents the operational scheduling and lifecycle of an appointment.

Its lifecycle is explicitly modeled.

```text
SCHEDULED
    │
    ▼
IN_PROGRESS
    │
    ▼
COMPLETED
```

Alternative outcomes include:

```text
SCHEDULED ──► CANCELLED

SCHEDULED ──► NO_SHOW
```

Bookings also distinguish between:

* **Expected Price**
* **Final Price**

The expected price represents what was anticipated at booking time.

The final price represents what was actually charged.

This distinction is important for accurate historical reporting.

---

# Payments

A Payment represents **actual money received**.

Payment is deliberately separate from Booking.

A payment may:

* Reference a booking
* Exist without a booking
* Be received in cash
* Be received through a non-cash method
* Represent an overpayment

This separation prevents the system from assuming:

> booking = payment

Those are different business events.

---

# Expenses

Expenses represent money spent by the business.

They are separate from client payments.

The financial model therefore distinguishes between:

```text
Money coming in
      │
      └── Payments


Money going out
      │
      └── Expenses
```

This distinction provides a cleaner foundation for financial reporting and cash reconciliation.

---

# Financial Events

Not every financial event belongs to a specific shop.

Ratibu therefore supports business-level financial events.

For example, an owner may inject cash into the business without that event belonging to a particular shop.

This prevents the domain model from artificially forcing every financial event into a shop.

---

# Cash Book

The Cash Book is responsible for tracking physical cash and end-of-day reconciliation.

Ratibu deliberately distinguishes:

* Money received
* Payment method
* Physical cash
* Cash-book records
* End-of-day reconciliation

Cash can also be recorded during the day rather than forcing the business to enter everything as one large end-of-day transaction.

The model includes explicit handling for conflicting end-of-day records, including the **supersede-and-flag** rule defined by the ADRs.

---

# Offline First

Offline operation is a fundamental architectural requirement.

It is not treated as an optional feature added later.

The client must be able to create operations while offline and synchronize them when connectivity becomes available.

The architecture therefore uses:

* Client-generated `StableId`s
* Idempotent synchronization
* Synchronization status
* Aggregate-aware conflict resolution
* Explicit domain rules
* Separation between local operations and server synchronization

Conceptually:

```text
              ┌───────────────┐
              │ Android / PWA │
              └───────┬───────┘
                      │
                 Local operation
                      │
                      ▼
              ┌───────────────┐
              │ Sync Engine   │
              └───────┬───────┘
                      │
              Synchronization
                      │
                      ▼
              ┌───────────────┐
              │ Domain Model  │
              └───────────────┘
```

The synchronization engine handles the generic mechanics.

Individual aggregates remain responsible for their own domain-specific conflict rules.

---

# Business Insights

Insights are treated as **derived information**, not authoritative business state.

The source of truth remains the underlying domain data.

```text
Bookings ──────┐
Payments ──────┤
Expenses ──────┼──► Business Insights
Cash Book ─────┤
Services ──────┘
```

This allows insights to be regenerated from the underlying data instead of maintaining another competing source of truth.

Potential insights include:

* Revenue trends
* Service performance
* Worker performance
* Shop performance
* Cash-flow information
* Booking trends

---

# Backend Architecture

The backend follows a consistent four-layer structure for each aggregate:

```text
aggregate/
│
├── domain/
├── application/
├── infrastructure/
└── api/
```

### Domain

Contains the business model:

* Aggregate roots
* Value objects
* Domain events
* Repository interfaces
* Business rules
* Invariants

The domain layer should remain independent of Spring, JPA, HTTP, and other infrastructure concerns.

### Application

Contains use cases and application services.

The application layer coordinates domain behavior without reaching into another aggregate's internal implementation.

### Infrastructure

Contains technical implementations such as:

* JPA entities
* Spring Data repositories
* Repository implementations
* Persistence mappers

### API

Contains:

* REST controllers
* Request DTOs
* Response DTOs

Controllers are intentionally kept thin.

---

# Current Backend Structure

```text
ratibu-backend/
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── ratibu/
        │           │
        │           ├── account/
        │           ├── booking/
        │           ├── business/
        │           ├── cashbook/
        │           ├── catalog/
        │           ├── client/
        │           ├── expense/
        │           ├── financeevent/
        │           ├── insight/
        │           ├── membership/
        │           ├── payment/
        │           ├── shop/
        │           ├── worker/
        │           │
        │           ├── common/
        │           │   ├── domain/
        │           │   ├── exception/
        │           │   └── web/
        │           │
        │           ├── config/
        │           ├── security/
        │           ├── sync/
        │           │
        │           └── RatibuApplication.java
        │
        └── resources/
            └── application.yml
```

Each aggregate currently has its four architectural layers scaffolded:

```text
business/
├── api/
├── application/
├── domain/
└── infrastructure/
```

The same pattern exists for the other aggregates.

At this stage, most of these packages contain architectural documentation (`package-info.java`) rather than completed feature implementations. This is intentional.

---

# Shared Domain Kernel

Shared domain primitives live under:

```text
com.ratibu.common.domain
```

Current primitives include:

```text
AggregateRoot
DomainEvent
Money
StableId
```

### `AggregateRoot`

Provides common aggregate identity and domain-event handling.

### `DomainEvent`

Represents meaningful events raised by aggregates.

### `Money`

Provides exact monetary representation and arithmetic without relying on floating-point calculations.

### `StableId`

Provides client-generatable identity suitable for an offline-first system.

---

# Synchronization

Synchronization is isolated under:

```text
com.ratibu.sync
```

It is responsible for generic synchronization concerns such as:

* Operation submission
* Idempotency
* Stable IDs
* Synchronization status
* Conflict dispatch

It does not own aggregate-specific business rules.

For example, Cash Book conflict resolution belongs to the Cash Book domain rather than the generic synchronization engine.

---

# Security

Security is isolated under:

```text
com.ratibu.security
```

and configuration concerns under:

```text
com.ratibu.config
```

The planned authentication model uses JWT-based authentication.

Authorization is based on the combination of:

```text
User Account
        +
Business Membership
        +
Role
        +
Shop Assignment
        +
Permission
```

Server-side authorization is the actual security boundary.

---

# Technology Stack

## Backend

* **Java 21**
* **Spring Boot 3.3.4**
* Spring Web
* Spring Validation
* Spring Data JPA
* Spring Security
* PostgreSQL
* Flyway
* JJWT
* Maven

## Clients

The backend is designed to serve:

* Android
* PWA / Web

Both clients are expected to participate in the offline-first synchronization model.

---

# Architectural Decision Records

The `docs/` directory contains the project's Architectural Decision Records.

The ADRs are a central part of the project rather than supplementary documentation.

They define decisions around:

* Aggregate boundaries
* Business and shop structure
* User accounts and memberships
* Workers
* Clients
* Services and catalog
* Booking lifecycle
* Payments
* Expenses
* Cash management
* Business-level financial events
* Insights
* Authorization
* Offline-first behavior
* Stable identifiers
* Synchronization
* Conflict resolution
* Layering and dependency direction

The ADRs provide the architectural context behind the structure found in the codebase.

---

# Development Philosophy

Ratibu 2.0 follows:

> **Understand the domain → make the architectural decision → document it → implement it.**

The project intentionally avoids beginning with a large collection of:

* Controllers
* Services
* Repositories
* JPA entities
* Database tables

without first establishing what those components actually represent.

The goal is to build a system where the code reflects the business rather than forcing the business into a generic CRUD structure.

---

# Development Roadmap

## Phase 1 — Architecture & Foundation

* [x] Define domain model
* [x] Define aggregate boundaries
* [x] Document architectural decisions
* [x] Define authorization model
* [x] Define offline-first strategy
* [x] Define financial rules
* [x] Create backend scaffold
* [x] Create Maven/Spring Boot project
* [x] Establish shared domain primitives
* [x] Establish aggregate package structure
* [x] Create application entry point

## Phase 2 — Domain Implementation

* [ ] Implement Business
* [ ] Implement Shop
* [ ] Implement Account
* [ ] Implement Membership
* [ ] Implement Worker
* [ ] Implement Client
* [ ] Implement Catalog
* [ ] Implement Booking
* [ ] Implement Payment
* [ ] Implement Expense
* [ ] Implement Financial Event
* [ ] Implement Cash Book
* [ ] Implement Insight

## Phase 3 — Persistence

* [ ] Define database schema
* [ ] Create Flyway migrations
* [ ] Implement JPA entities
* [ ] Implement repository adapters
* [ ] Implement persistence mappers
* [ ] Add persistence integration tests

## Phase 4 — Application Layer

* [ ] Implement use cases
* [ ] Implement application services
* [ ] Implement aggregate coordination
* [ ] Add application-layer tests

## Phase 5 — API & Security

* [ ] Implement REST APIs
* [ ] Implement request validation
* [ ] Implement JWT authentication
* [ ] Implement authorization
* [ ] Add API integration tests

## Phase 6 — Synchronization

* [ ] Implement sync operation model
* [ ] Implement idempotency
* [ ] Implement sync status tracking
* [ ] Implement conflict dispatch
* [ ] Implement aggregate-specific conflict rules
* [ ] Add offline synchronization tests

## Phase 7 — Clients

* [ ] Android client
* [ ] Local persistence
* [ ] Offline operation queue
* [ ] Synchronization client
* [ ] PWA client

## Phase 8 — Business Intelligence

* [ ] Revenue insights
* [ ] Service insights
* [ ] Worker insights
* [ ] Shop insights
* [ ] Cash-flow insights
* [ ] Business dashboard

---

# Repository

```text
Ratibu2.0/
│
├── docs/                    # Architectural Decision Records
├── LICENSE
├── scaffold.sh              # Backend architecture scaffold
│
└── ratibu-backend/
    ├── pom.xml
    └── src/
```

---

# Current Goal

The immediate goal is **not to implement every feature at once**.

The goal is to establish a repeatable implementation pattern using the architecture already defined.

The next stage is to take the first aggregate through the complete lifecycle:

```text
Domain
   ↓
Application
   ↓
Infrastructure
   ↓
API
   ↓
Tests
```

Once that pattern has been validated, it can be applied deliberately to the remaining aggregates.

---

## Ratibu 2.0

**Architecture first. Domain-driven. Offline-first. Built for the way small businesses actually operate.**
