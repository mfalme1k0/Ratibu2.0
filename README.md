# Ratibu 2.0

> **An offline-first business management platform for small businesses.**

Ratibu is a business management and financial tracking platform designed to help small businesses manage their day-to-day operations from one place.

It brings together business management, multiple shop locations, workers, clients, services, bookings, payments, expenses and cash management.

Ratibu 2.0 is a ground-up architectural redesign of the original Ratibu concept. Its focus is to establish a reliable domain model, clear business rules and an extensible architecture before building out the application features.

**Project status:** Active development — architecture complete, backend vertical slices in progress. Business, Shop and Account are implemented end to end; the remaining aggregates are scaffolded.

---

## Table of Contents

- [The Problem](#the-problem)
- [Project Vision](#project-vision)
- [Current Status](#current-status)
- [Core Domain Model](#core-domain-model)
- [Architectural Principles](#architectural-principles)
- [Offline-First Architecture](#offline-first-architecture)
- [Backend Architecture](#backend-architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Testing](#testing)
- [Architectural Decision Records](#architectural-decision-records)
- [Development Roadmap](#development-roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## The Problem

Many small businesses operate using a combination of:

- Paper-based records and cash books.
- WhatsApp conversations.
- Appointment books and spreadsheets.
- Separate customer and worker records.
- Manual payment and expense tracking.

As a business grows, keeping these records consistent becomes increasingly difficult.

Business owners need reliable answers to questions such as:

- How much money did the business receive today?
- How much physical cash should be available?
- Which services generate the most revenue?
- Which workers are responsible for particular services?
- Which bookings have been completed or cancelled?
- Which payments are outstanding?
- How are individual shop locations performing?
- What happened when the business was offline?

Ratibu aims to provide a unified system for answering these questions without assuming that a business always has reliable internet connectivity.

## Project Vision

Ratibu is being designed to support:

1. **Business management** — manage business information, workers, clients and services.
2. **Multi-shop operations** — manage multiple locations under one business.
3. **Booking management** — organize appointments and track their lifecycle.
4. **Financial tracking** — record payments, expenses and other financial events.
5. **Cash management** — maintain cash books and support end-of-day reconciliation.
6. **Offline operation** — allow clients to continue working without connectivity and synchronize when online.
7. **Business insights** — derive useful operational and financial information from business records.

The platform is intended to serve both Android and Progressive Web App (PWA) clients through a shared backend.

---

## Current Status

Ratibu is built one aggregate at a time as **vertical slices**: domain, application, persistence, REST API and tests land together.

| Aggregate | Status | Notes |
|---|---|---|
| Business | Implemented | Domain, `CreateBusiness` use case, JPA persistence, REST endpoint, unit and E2E tests. |
| Shop | Implemented | Belongs to a Business; shop status modelled in the domain. |
| Account | Implemented | Unique email business key; client-supplied `StableId` supported. |
| Membership, Worker, Client, Catalog, Booking, Payment, Expense, Financial Event, Cash Book, Insight, Sync | Scaffolded | Package structure only (`package-info.java`); no behaviour yet. |

### Available API

All endpoints are under `/api/v1`. Create requests accept an optional client-generated ID (a UUID string) so offline clients can retry safely.

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/businesses` | Create a business (`name`, `currency` as ISO 4217 code, optional `businessId`). |
| `POST` | `/api/v1/businesses/{businessId}/shops` | Create a shop within a business (`name`, optional `shopId`). |
| `POST` | `/api/v1/accounts` | Create an account (`email`, optional `displayName`, optional `accountId`). |

Errors use a uniform body: `timestamp`, `status`, `error`, `message`, `path` and `details`.

> **Security note:** `SecurityConfig` is stateless with CSRF disabled and requires authentication for every request except the three `POST` endpoints above and `/error`. JWT issuance and validation, and membership-based authorization, are not implemented yet, so those create endpoints are intentionally open for now.

Database migrations currently cover `business`, `shop` and `account` (`V1`–`V3` under `db/migration`).

---

## Core Domain Model

Ratibu is organized around business concepts rather than generic CRUD resources.

The domain model currently identifies the following aggregates:

| Aggregate | Responsibility |
|---|---|
| Business | Represents the business organization. |
| Shop | Represents an operational location. |
| Account | Represents a user's identity. |
| Membership | Defines a user's relationship with a business. |
| Worker | Manages business worker profiles and capabilities. |
| Client | Represents business customers. |
| Catalog | Manages services and their configuration. |
| Booking | Manages appointments and their lifecycle. |
| Payment | Records money received. |
| Expense | Records business expenditure. |
| Financial Event | Records other business-level financial activity. |
| Cash Book | Tracks physical cash and reconciliation. |
| Insight | Provides derived business information. |

Each aggregate is responsible for its own state, business rules and invariants.

Aggregates communicate through defined application boundaries, stable identities and domain events rather than directly modifying one another's internal state.

### Business and Shop Structure

A Business represents the organizational boundary, while a Shop represents an operational and financial boundary within that business.

A business can start with one shop and expand to multiple locations.

```text
Business
├── Shop A
├── Shop B
└── Shop C
```

This structure allows Ratibu to support both independent businesses and growing multi-location operations.

### Identity and Membership

Ratibu separates user authentication from business participation.

- **Account:** Represents a person's identity.
- **Membership:** Represents their relationship with a particular business.
- **Role and permissions:** Define what they can do.
- **Shop assignments:** Define the locations they can access.

A user does not automatically gain access to every business in the system.

### Bookings and Payments

Bookings and payments are separate domain concepts.

A booking represents an appointment and its operational lifecycle.

```text
SCHEDULED
    |
    v
IN_PROGRESS
    |
    v
COMPLETED
```

Alternative outcomes include:

- CANCELLED
- NO_SHOW

A booking also distinguishes between its expected price and final price.

A payment represents actual money received. It can reference a booking or exist independently.

This distinction is important because:

**A booking does not automatically mean that a payment has been received.**

### Financial Management

Ratibu distinguishes between:

- Payments — money received.
- Expenses — money spent.
- Financial events — other business-level financial activity.
- Cash book entries — physical cash movements and reconciliation.

The financial model is designed to support accurate reporting without confusing expected revenue, actual payments and physical cash.

---

## Architectural Principles

Ratibu 2.0 follows an architecture-first development approach.

The guiding principle is:

**Understand the domain → Define the architecture → Document decisions → Implement → Test.**

### Domain-Driven Design

The application is organized around business domains and aggregate boundaries.

Each aggregate owns its business rules and protects its invariants.

The database schema and framework implementation should support the domain model rather than dictate it.

### Separation of Concerns

The backend follows four architectural layers:

- **Domain:** Business entities, value objects, aggregate roots, domain events and invariants.
- **Application:** Use cases and application services.
- **Infrastructure:** Persistence, repository implementations and technical integrations.
- **API:** REST controllers, request DTOs and response DTOs.

The domain layer is designed to remain independent of Spring, JPA and HTTP concerns.

### Explicit Business Rules

Important business decisions are documented instead of being left to incidental implementation details.

Examples include:

- Booking lifecycle transitions.
- Payment and booking separation.
- Historical pricing.
- Shop-level financial boundaries.
- Membership-based authorization.
- Cash reconciliation.
- Offline synchronization and conflict resolution.

---

## Offline-First Architecture

Offline operation is a fundamental architectural requirement, not an optional feature.

Ratibu is designed around the assumption that businesses may have unreliable or intermittent internet connectivity.

The synchronization architecture is based on:

- Client-generated `StableId` identifiers.
- Idempotent synchronization operations.
- Synchronization status tracking.
- Explicit conflict handling.
- Aggregate-aware conflict resolution.
- Separation of synchronization mechanics from business rules.

Conceptually:

```text
        Android / PWA
              |
              v
       Local Operations
              |
              v
        Sync Engine
              |
              v
       Backend API
              |
              v
        Domain Model
              |
              v
          Database
```

The synchronization engine handles generic synchronization concerns.

Individual aggregates remain responsible for their own business-specific conflict rules.

For example, cash-book reconciliation conflicts belong to the Cash Book domain rather than the generic synchronization engine.

---

## Backend Architecture

Each aggregate follows a consistent package structure:

```text
aggregate/
├── api/
├── application/
├── domain/
└── infrastructure/
```

### Domain Layer

Contains:

- Aggregate roots.
- Entities and value objects.
- Domain events.
- Repository interfaces.
- Business rules and invariants.

### Application Layer

Contains:

- Use cases.
- Application services.
- Aggregate coordination.
- Transaction boundaries.

### Infrastructure Layer

Contains:

- JPA entities.
- Spring Data repositories.
- Repository adapters.
- Persistence mappings.

### API Layer

Contains:

- REST controllers.
- Request DTOs.
- Response DTOs.
- API-level validation.

Controllers are intended to remain thin, delegating business operations to the application layer.

### Shared Domain Kernel

Shared domain primitives are located under:

`com.ratibu.common.domain`

| Component | Responsibility |
|---|---|
| `AggregateRoot` | Common aggregate identity and domain-event handling. |
| `DomainEvent` | Represents meaningful domain occurrences. |
| `Money` | Provides precise monetary representation and arithmetic. |
| `StableId` | Provides stable, client-generatable identifiers for offline operations. |

These primitives provide common foundations without forcing unrelated aggregates to share business logic.

---

## Technology Stack

### Backend

| Technology | Purpose |
|---|---|
| Java 21 | Primary programming language |
| Spring Boot 3.3.4 | Application framework |
| Spring Web | REST API development |
| Spring Validation | Request validation |
| Spring Data JPA | Persistence abstraction |
| Spring Security | Authentication and authorization |
| PostgreSQL | Relational database |
| Flyway | Database schema migrations |
| JJWT 0.12.6 | JWT infrastructure |
| Lombok | Boilerplate reduction |
| Maven | Build and dependency management |

### Testing

- JUnit 5 through Spring Boot Test.
- Spring Security Test.
- H2 for database testing.
- JaCoCo for coverage reporting.

### Planned Clients

- Android application.
- Progressive Web Application (PWA).

---

## Project Structure

The repository is organized to separate architectural documentation from backend implementation.

```text
Ratibu2.0/
│
├── docs/
│   └── decisions/
│       └── Architectural Decision Records
│
├── ratibu-backend/
│   ├── pom.xml
│   │
│   └── src/
│       ├── main/
│       │   ├── java/com/ratibu/
│       │   │   ├── account/
│       │   │   ├── booking/
│       │   │   ├── business/
│       │   │   ├── cashbook/
│       │   │   ├── catalog/
│       │   │   ├── client/
│       │   │   ├── expense/
│       │   │   ├── financeevent/
│       │   │   ├── insight/
│       │   │   ├── membership/
│       │   │   ├── payment/
│       │   │   ├── shop/
│       │   │   ├── worker/
│       │   │   │
│       │   │   ├── common/
│       │   │   ├── config/
│       │   │   ├── security/
│       │   │   ├── sync/
│       │   │   │
│       │   │   └── RatibuApplication.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/
│       │           ├── V1__business.sql
│       │           ├── V2__shop.sql
│       │           └── V3__account.sql
│       │
│       └── test/
│           ├── java/com/ratibu/
│           └── resources/
│               ├── application-test.yml
│               └── application-e2e.yml
│
├── .gitignore
├── CONTRIBUTING.md
├── LICENSE
└── README.md
```

The aggregate packages establish architectural boundaries. Business, Shop and Account are fully implemented; the other aggregate packages currently contain only `package-info.java` placeholders (see [Current Status](#current-status)).

---

## Getting Started

### Prerequisites

Make sure you have the following installed:

- Java Development Kit (JDK) 21.
- Apache Maven 3.9 or newer.
- PostgreSQL (only needed to run the application; the test suite uses in-memory H2).
- Git.

The repository does not currently include the Maven Wrapper, so Maven must be installed separately.

### 1. Clone the repository

```bash
git clone https://github.com/mfalme1k0/Ratibu2.0.git
```

### 2. Navigate to the backend

```bash
cd Ratibu2.0/ratibu-backend
```

### 3. Configure PostgreSQL

Create a PostgreSQL database named `ratibu` and configure a database user with appropriate permissions.

The default local configuration currently expects:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ratibu
    username: ratibu
    password: ratibu
```

Adjust these values to match your local PostgreSQL setup.

### 4. Configure application secrets

The local JWT configuration contains a development placeholder.

Replace it with a properly generated secret for local testing. Never use the placeholder or commit production credentials to version control.

### 5. Build the project

```bash
mvn clean package
```

### 6. Run the application

```bash
mvn spring-boot:run
```

The application is configured to use port `8080`. Once it is running, you can try the API:

```bash
curl -X POST http://localhost:8080/api/v1/businesses \
  -H "Content-Type: application/json" \
  -d '{"name": "Mama Njeri Salon", "currency": "KES"}'
```

Database migrations are managed by Flyway, while Hibernate validates the schema rather than generating it automatically.

---

## Configuration

The primary configuration file is:

`ratibu-backend/src/main/resources/application.yml`

Important configuration properties include:

| Property | Purpose |
|---|---|
| `spring.datasource.url` | PostgreSQL connection URL |
| `spring.datasource.username` | Database username |
| `spring.datasource.password` | Database password |
| `spring.flyway.enabled` | Enables database migrations |
| `server.port` | Application server port |
| `ratibu.jwt.secret` | JWT signing secret |
| `ratibu.jwt.access-token-ttl-minutes` | Access token lifetime |
| `ratibu.jwt.refresh-token-ttl-days` | Refresh token lifetime |

For non-local environments, secrets should be supplied through environment-specific configuration or a secrets manager.

---

## Testing

Run the complete backend test suite from `ratibu-backend/`:

```bash
mvn clean test
```

The suite does not need PostgreSQL. Tests run against in-memory H2 (in PostgreSQL mode) using Flyway migrations, so the real schema is exercised.

Current test layers, per aggregate:

- **Domain:** aggregate behaviour, value objects and invariants.
- **Application:** use-case services against in-memory repositories.
- **Infrastructure:** repository adapters and persistence mappings.
- **API:** controller tests for request validation and error responses.
- **End-to-end:** full-stack tests with the real `SecurityConfig` (`application-e2e.yml`).

The shared kernel (`Money`, `StableId`) and `ApiError` are tested as well.

### Coverage

JaCoCo generates a coverage report during `mvn test`. To enforce the coverage gate locally or before a pull request:

```bash
mvn verify -Pcoverage
```

The gate requires at least 80% line coverage and 70% branch coverage. These thresholds are deliberately progressive and may be raised as more aggregates land.

---

## Architectural Decision Records

The `docs/decisions/` directory contains the Architectural Decision Records (ADRs).

These documents are a core part of the project.

They explain the reasoning behind decisions involving:

- Aggregate boundaries.
- Business and shop relationships.
- Accounts and memberships.
- Workers and clients.
- Catalog and services.
- Booking lifecycle.
- Payments and expenses.
- Financial events.
- Cash-book management.
- Business insights.
- Authentication and authorization.
- Offline-first architecture.
- Stable identifiers.
- Synchronization and conflict resolution.
- Layering and dependency direction.

The ADRs serve as the architectural reference when implementing new functionality.

---

## Development Roadmap

Ratibu is being developed incrementally, with emphasis on preserving domain boundaries and validating architectural decisions.

### Phase 1 — Architecture and Foundation

- [x] Define the domain model.
- [x] Establish aggregate boundaries.
- [x] Document architectural decisions.
- [x] Define the authorization model.
- [x] Define the offline-first strategy.
- [x] Establish financial domain rules.
- [x] Create the Spring Boot backend.
- [x] Establish the aggregate package structure.
- [x] Introduce shared domain primitives.
- [x] Establish the application entry point.
- [x] Add test infrastructure and a coverage gate.

### Phase 2 — Domain Implementation

- [x] Business and Shop.
- [x] Account.
- [ ] Membership.
- [ ] Worker and Client.
- [ ] Catalog and Services.
- [ ] Booking.
- [ ] Payment.
- [ ] Expense.
- [ ] Financial Event.
- [ ] Cash Book.
- [ ] Insight.

### Phase 3 — Persistence

- [x] Flyway migrations for Business, Shop and Account.
- [x] JPA entities, repository adapters and mappings for Business, Shop and Account.
- [x] Persistence tests for Business, Shop and Account.
- [ ] Persistence for the remaining aggregates.

### Phase 4 — Application Layer

- [x] Create use cases for Business, Shop and Account.
- [x] Application-layer tests for these use cases.
- [ ] Use cases for the remaining aggregates.
- [ ] Establish cross-aggregate coordination.

### Phase 5 — API and Security

- [x] REST create endpoints and request validation for Business, Shop and Account.
- [x] Uniform API error responses.
- [x] Stateless security baseline.
- [x] API and end-to-end tests for these endpoints.
- [ ] Read, update and lifecycle endpoints.
- [ ] Implement JWT authentication.
- [ ] Implement membership-based authorization.
- [ ] REST APIs for the remaining aggregates.

### Phase 6 — Synchronization

- [ ] Implement synchronization operations.
- [ ] Implement idempotency.
- [ ] Implement synchronization status tracking.
- [ ] Implement conflict dispatch.
- [ ] Implement aggregate-specific conflict resolution.
- [ ] Add offline synchronization tests.

### Phase 7 — Client Applications

- [ ] Android application.
- [ ] Local persistence.
- [ ] Offline operation queue.
- [ ] Synchronization client.
- [ ] Progressive Web Application.

### Phase 8 — Business Insights

- [ ] Revenue insights.
- [ ] Service performance.
- [ ] Worker performance.
- [ ] Shop performance.
- [ ] Cash-flow information.
- [ ] Business dashboard.

*Roadmap items represent development intentions, not necessarily completed functionality.*

---

## Contributing

Ratibu 2.0 is an evolving project.

Contributions should respect the existing architectural decisions and domain boundaries. Please read [CONTRIBUTING.md](CONTRIBUTING.md) for setup, workflow, coding and testing guidelines.

Before introducing a significant architectural change:

1. Understand the existing domain model.
2. Review the relevant ADRs.
3. Identify the affected aggregates and boundaries.
4. Document significant architectural changes.
5. Implement and test the change.

The goal is to maintain a codebase where business rules remain understandable, testable and independent of infrastructure concerns.

---

## License

This project is licensed under the MIT License.

See [LICENSE](LICENSE) for details.

---

## Ratibu 2.0

**Built around the realities of running a small business.**

The long-term goal is to give small businesses a dependable way to manage their operations, understand their finances and continue working even when connectivity is unreliable.
