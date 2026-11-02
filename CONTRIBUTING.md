# Contributing to Ratibu 2.0

Thank you for your interest in contributing to Ratibu 2.0.

Ratibu is an offline-first business management platform designed to help small businesses manage their operations, bookings and finances.

The project is being developed with a strong emphasis on domain-driven design, clear architectural boundaries, maintainability and reliable business rules.

Contributions are welcome, but changes should preserve the architectural direction of the project.

This document explains how to contribute effectively.

---

## Table of Contents

- [Before You Start](#before-you-start)
- [Development Philosophy](#development-philosophy)
- [Project Architecture](#project-architecture)
- [Working with Domain Aggregates](#working-with-domain-aggregates)
- [Architectural Decision Records](#architectural-decision-records)
- [Development Setup](#development-setup)
- [Development Workflow](#development-workflow)
- [Coding Guidelines](#coding-guidelines)
- [Testing Requirements](#testing-requirements)
- [Database and Migrations](#database-and-migrations)
- [Git Workflow](#git-workflow)
- [Pull Request Guidelines](#pull-request-guidelines)
- [Commit Messages](#commit-messages)
- [Reporting Issues](#reporting-issues)

---

## Before You Start

Before contributing:

1. Read the main [README](README.md).
2. Understand the project's goals and current development status.
3. Review the relevant Architectural Decision Records in `docs/decisions/`.
4. Check existing issues and open pull requests to avoid duplicating work.
5. Discuss significant architectural changes before implementing them.

Not every planned feature is already implemented. Check the existing codebase before assuming that a domain or capability is available.

---

## Development Philosophy

Ratibu follows an architecture-first development approach.

The preferred development sequence is:

```text
Understand the requirement
          |
          v
Identify the domain
          |
          v
Review architectural decisions
          |
          v
Define business rules
          |
          v
Design the implementation
          |
          v
Implement
          |
          v
Write and run tests
          |
          v
Review and submit
```

The following principles guide development.

### 1. Domain before infrastructure

Business rules should belong to the domain model, not controllers, database entities or framework-specific components.

### 2. Explicit boundaries

Aggregates should protect their own invariants.

One aggregate should not directly manipulate another aggregate's internal state.

### 3. Prefer composition over unnecessary coupling

Shared functionality should be introduced deliberately. Avoid creating dependencies between unrelated domains merely to reuse a small amount of code.

### 4. Make business rules explicit

Important decisions should be represented in code and tests rather than hidden in controller logic or database side effects.

### 5. Design for offline operation

Assume that clients may temporarily lose connectivity.

Identifiers, operations and synchronization behavior should support reliable processing when connectivity returns.

### 6. Keep the architecture understandable

Avoid unnecessary abstractions, speculative frameworks and premature optimization.

---

## Project Architecture

The backend is organized around four primary layers:

```text
aggregate/
├── api/
├── application/
├── domain/
└── infrastructure/
```

### Domain

Contains:

- Aggregate roots.
- Entities.
- Value objects.
- Domain events.
- Repository interfaces.
- Business rules and invariants.

The domain layer should not depend on Spring, JPA, HTTP or database-specific implementations.

### Application

Contains:

- Use cases.
- Application services.
- Transaction coordination.
- Aggregate orchestration.

Application services coordinate domain behavior without becoming repositories for business logic.

### Infrastructure

Contains:

- JPA entities.
- Spring Data repositories.
- Repository implementations.
- Persistence mappings.
- External technical integrations.

Infrastructure implements technical requirements defined by the application and domain layers.

### API

Contains:

- REST controllers.
- Request DTOs.
- Response DTOs.
- HTTP-level validation and mapping.

Controllers should remain thin and delegate application behavior to the application layer.

---

## Working with Domain Aggregates

Ratibu uses aggregate boundaries to protect business consistency.

Each aggregate owns its state and business rules.

Examples of domain areas include:

| Domain | Responsibility |
|---|---|
| Account | User identity |
| Membership | Business participation and access |
| Business | Business organization |
| Shop | Operational location |
| Worker | Worker management |
| Client | Customer management |
| Catalog | Services and offerings |
| Booking | Appointment lifecycle |
| Payment | Money received |
| Expense | Business expenditure |
| Financial Event | Other financial activity |
| Cash Book | Cash tracking and reconciliation |
| Insight | Derived business information |

### Aggregate rules

When implementing or modifying an aggregate:

1. Identify its root entity.
2. Define which state it owns.
3. Identify its invariants.
4. Define the operations through which its state can change.
5. Keep persistence concerns outside the domain model.
6. Use stable identifiers when referencing other aggregates.
7. Publish domain events when meaningful business events occur.

### Cross-aggregate communication

Avoid directly modifying another aggregate's internal entities.

Prefer:

- Stable identifiers.
- Application-level coordination.
- Explicit use cases.
- Domain events where appropriate.

For example, a booking should reference a client using an identifier rather than owning and modifying the client's entire aggregate.

### Financial consistency

Financial concepts must remain distinct.

In particular:

- A booking is not a payment.
- Expected revenue is not received revenue.
- Physical cash is not automatically equivalent to all recorded payments.
- A cash-book entry must represent a meaningful cash movement.

Use the shared `Money` value object for monetary calculations rather than floating-point types.

---

## Architectural Decision Records

Architectural Decision Records (ADRs) are an important part of Ratibu's development process.

They document why architectural decisions were made and provide guidance for future implementation.

ADRs are located in:

```text
docs/decisions/
```

Before implementing a change that affects architecture:

1. Review the relevant ADRs.
2. Determine whether the proposed implementation follows existing decisions.
3. Identify any conflicts with current architecture.
4. Propose an ADR if a significant decision needs to change.

### When to create an ADR

An ADR is appropriate when changing or introducing:

- Aggregate boundaries.
- Domain ownership.
- Persistence strategies.
- Authentication and authorization.
- Synchronization behavior.
- Conflict resolution.
- Financial consistency rules.
- Cross-domain communication.
- Major technology choices.

Avoid creating ADRs for routine implementation details.

---

## Development Setup

### Prerequisites

- JDK 21.
- PostgreSQL.
- Git.

The backend includes Maven Wrapper.

### 1. Fork and clone the repository

```bash
git clone https://github.com/mfalme1k0/Ratibu2.0.git

cd Ratibu2.0
```

### 2. Navigate to the backend

```bash
cd ratibu-backend
```

### 3. Configure PostgreSQL

Create a local PostgreSQL database and configure your application properties.

Do not commit personal database credentials or secrets.

### 4. Build the application

```bash
./mvnw clean package
```

### 5. Run the application

```bash
./mvnw spring-boot:run
```

---

## Development Workflow

For each contribution:

### Step 1: Identify the task

Understand the expected behavior before changing code.

If requirements are unclear, discuss them before implementation.

### Step 2: Inspect the existing implementation

Review:

- Relevant aggregate.
- Existing application services.
- Repository interfaces.
- Database migrations.
- Tests.
- Related ADRs.

Avoid introducing duplicate functionality.

### Step 3: Define the change

Identify:

- Which layer needs modification.
- Which business rules are affected.
- Whether existing invariants change.
- Whether a database migration is required.
- Which tests need to be added.

### Step 4: Implement incrementally

Keep changes focused.

Avoid unrelated refactoring in feature pull requests.

### Step 5: Test

Run the relevant tests and the complete test suite.

### Step 6: Review your changes

Check the diff for:

- Unnecessary files.
- Generated build artifacts.
- Debugging statements.
- Exposed credentials.
- Unrelated modifications.
- Missing tests.

### Step 7: Submit a pull request

Provide a clear explanation of the change and its impact.

---

## Coding Guidelines

### Java

- Use Java 21 language features where appropriate.
- Prefer descriptive names over abbreviations.
- Keep methods focused on one responsibility.
- Avoid unnecessary static state.
- Prefer immutable value objects where practical.
- Use constructor injection.
- Avoid exposing mutable internal collections.
- Handle exceptional conditions explicitly.

### Domain modeling

- Keep business logic inside the appropriate domain.
- Protect aggregate invariants.
- Prefer explicit domain methods over unrestricted setters.
- Use value objects for concepts with their own validation and behavior.
- Avoid leaking persistence entities into the domain layer.

### Monetary values

- Use `BigDecimal` or the shared `Money` abstraction.
- Never use `float` or `double` for financial calculations.
- Define rounding behavior explicitly.
- Distinguish between expected amounts and actual transactions.

### Naming

Prefer names that communicate intent.

For example:

```java
completeBooking()
recordPayment()
reconcileCashBook()
cancelBooking()
```

Avoid vague methods such as:

```java
process()
update()
handle()
execute()
```

unless the surrounding abstraction makes their meaning explicit.

### Comments

Comments should explain:

- Why a decision exists.
- Why an implementation is necessary.
- Important domain constraints.
- Non-obvious technical behavior.

Avoid comments that simply repeat what the code already says.

---

## Testing Requirements

Tests are part of the implementation, not an optional follow-up.

Contributions should include tests appropriate to the affected layer.

### Domain tests

Verify:

- Business rules.
- Aggregate invariants.
- Valid state transitions.
- Invalid operations.
- Value-object behavior.
- Domain-event generation.

### Application tests

Verify:

- Use-case behavior.
- Aggregate coordination.
- Transaction boundaries.
- Failure handling.
- Authorization requirements where applicable.

### Infrastructure tests

Verify:

- Persistence mappings.
- Repository behavior.
- Database constraints.
- Migration compatibility.

### API tests

Verify:

- HTTP status codes.
- Request validation.
- Response contracts.
- Authentication and authorization.

### Run the test suite

```bash
./mvnw clean test
```

Before submitting a pull request, ensure the complete test suite passes.

Do not remove or weaken existing tests simply to make a build pass.

---

## Database and Migrations

Ratibu uses PostgreSQL and Flyway for database schema management.

All schema changes should be managed through versioned Flyway migrations.

Guidelines:

1. Never rely on Hibernate to generate production schemas.
2. Do not modify an already-applied migration to change an existing database.
3. Create a new migration for subsequent schema changes.
4. Preserve existing data whenever possible.
5. Include appropriate constraints and indexes.
6. Ensure persistence changes remain consistent with domain invariants.
7. Test migrations against the expected database configuration.

Hibernate schema validation should remain enabled.

---

## Git Workflow

Use feature branches rather than committing directly to `main`.

### Branch naming

Recommended conventions:

| Branch type | Example |
|---|---|
| Feature | `feature/booking-management` |
| Bug fix | `fix/payment-validation` |
| Refactoring | `refactor/domain-primitives` |
| Documentation | `docs/update-contributing-guide` |
| Tests | `test/booking-lifecycle` |
| Architecture | `architecture/sync-design` |

### Creating a branch

```bash
git checkout main
git pull origin main

git checkout -b feature/your-feature-name
```

### Keeping your branch updated

```bash
git fetch origin
git merge origin/main
```

Resolve conflicts carefully.

Generated files, including Maven's `target/` directory, should not be committed.

### Checking your changes

```bash
git status
git diff
git diff --check
```

### Pushing your branch

```bash
git push -u origin feature/your-feature-name
```

---

## Pull Request Guidelines

Every pull request should explain the purpose and scope of the change.

Include:

### 1. Summary

What does this change introduce or fix?

### 2. Motivation

Why is the change necessary?

### 3. Implementation

Which domains, layers and components were affected?

### 4. Testing

Which tests were added or executed?

### 5. Architectural impact

Does this change affect an existing ADR or require a new one?

### 6. Additional information

Include screenshots, API examples or migration notes where relevant.

### Pull request checklist

Before submitting, verify:

- [ ] The change addresses the intended issue.
- [ ] Existing architectural decisions have been reviewed.
- [ ] Domain boundaries are respected.
- [ ] Business rules are implemented in the appropriate layer.
- [ ] Relevant tests have been added.
- [ ] The complete test suite passes.
- [ ] Database changes include appropriate Flyway migrations.
- [ ] No credentials or secrets are committed.
- [ ] No generated build artifacts are included.
- [ ] Documentation has been updated where necessary.
- [ ] The pull request is focused and clearly described.

---

## Commit Messages

Use clear, descriptive commit messages.

Ratibu follows a Conventional Commits-inspired format:

```text
type(scope): description
```

Examples:

```text
feat(booking): introduce booking lifecycle

feat(payment): add payment recording

fix(cashbook): validate reconciliation balance

test(booking): cover invalid state transitions

refactor(domain): improve stable identifier handling

docs(architecture): document aggregate boundaries

chore(build): update Maven dependencies
```

Common types:

| Type | Purpose |
|---|---|
| `feat` | New functionality |
| `fix` | Bug fixes |
| `test` | Test additions or improvements |
| `refactor` | Internal code improvements |
| `docs` | Documentation changes |
| `chore` | Maintenance and tooling |
| `architecture` | Architectural changes |

---

## Reporting Issues

When reporting a bug, include:

- A clear description of the problem.
- Steps to reproduce it.
- Expected behavior.
- Actual behavior.
- Relevant error messages or stack traces.
- Environment information where relevant.

For feature requests, explain:

- The problem being addressed.
- The expected business behavior.
- The affected domain.
- Any relevant architectural considerations.

Avoid submitting feature requests that prescribe an implementation without first explaining the underlying problem.

---

## Final Note

Ratibu is being built around the realities of running a small business, including limited resources, multiple operational responsibilities and unreliable connectivity.

The goal is not simply to build more features.

It is to build a dependable system whose business rules, financial records and operational workflows can be trusted.

Every contribution should move the project closer to that goal.

**Thank you for helping build Ratibu.**