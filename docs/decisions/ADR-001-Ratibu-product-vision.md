# ADR-001: Ratibu Product Vision and Core Business Model

**Status:** Proposed
**Date:** 2026-08-31
**Decision Type:** Product / Architecture
**Project:** Ratibu

---

## 1. Context

Ratibu is being redesigned as a Java-based Android application, building upon the original Ratibu application while expanding its capabilities.

The original concept focused primarily on business bookings and presenting business insights. During the redesign, the scope was expanded to address a broader set of problems faced by small and growing businesses:

* Managing bookings and appointments
* Managing clients
* Managing workers
* Managing services
* Recording business income and expenses
* Maintaining a cash book
* Recording transactions that occur outside the application
* Reconciling expected and actual end-of-day cash
* Supporting businesses with multiple shops or branches
* Providing business insights based on operational and financial data

The system should support both an individual business owner operating a single shop and an owner managing a larger business with multiple shops and potentially up to approximately 50 workers.

The system should not require every business transaction to originate from a Ratibu booking. Businesses frequently receive customers through walk-ins, phone calls, WhatsApp, physical bookings, or other channels. Ratibu therefore needs a way to capture business activity that occurs outside the application.

At the same time, simply storing business data provides limited value. Ratibu should use the collected information to provide understandable, actionable business insights to the owner.

---

## 2. Problem

A basic booking application only answers questions such as:

* How many bookings do I have?
* Who is booked today?
* Which client made a booking?

A basic cash book answers questions such as:

* How much cash came in?
* How much cash went out?
* What is my current balance?

However, these systems are more valuable when they are connected.

For example, a completed booking and payment should be reflected in the business's financial records without requiring the merchant to enter the same information twice.

At the same time, Ratibu must account for transactions that did not originate from a booking.

The system should therefore connect:

**Business operations → Financial activity → Business intelligence**

while keeping these concepts distinct enough to correctly handle cases such as unpaid bookings, partial payments, offline sales, expenses, refunds, and reconciliation.

---

## 3. Decision

Ratibu will be designed as an **offline-first business management and intelligence platform for small and growing service-based businesses**.

The core product will combine:

1. **Business operations**
2. **Financial management**
3. **Business intelligence**

The system will use a shared core model that supports both single-shop and multi-shop businesses rather than maintaining separate applications or fundamentally different codebases.

---

# 4. Product Vision

Ratibu's working product vision is:

> **Ratibu helps small and growing businesses manage bookings, customers, workers, shops, and cash flow while turning everyday business activity into useful insights for the business owner.**

The primary value of Ratibu is not simply recording business information, but helping the owner understand what is happening in their business and make better decisions.

---

# 5. Core Product Pillars

## 5.1 Business Operations

Ratibu will manage the operational side of the business, including:

* Businesses
* Shops/branches
* Workers
* Clients
* Services
* Bookings
* Appointment scheduling
* Booking status
* Service completion

---

## 5.2 Financial Management

Ratibu will maintain a simple, business-oriented financial view including:

* Payments
* Cash-in transactions
* Cash-out transactions
* Expenses
* Offline sales
* End-of-day entries
* Cash reconciliation
* Shop-level cash books
* Business-level financial summaries

Ratibu will provide financial visibility without attempting to replace full professional accounting software.

The goal is to help an owner answer:

> **How is my business doing financially?**

rather than attempting to provide a complete statutory accounting system.

---

## 5.3 Business Intelligence

Ratibu will transform operational and financial data into useful information.

Potential insight categories include:

### Financial

* Revenue
* Expenses
* Net cash movement
* Average transaction value
* Cash-flow trends
* Best and worst performing periods

### Bookings

* Number of bookings
* Completion rate
* Cancellation rate
* No-show rate
* Peak hours
* Peak days
* Most popular services

### Clients

* New clients
* Returning clients
* Client retention
* Client spending
* High-value clients
* Inactive clients

### Workers

* Bookings per worker
* Revenue per worker
* Average transaction value
* Worker utilization
* Worker performance comparisons

### Shops

* Revenue per shop
* Bookings per shop
* Expenses per shop
* Cash position
* Growth rate
* Shop comparisons

---

# 6. Business Structure

Ratibu will model the business hierarchy as:

```text
Owner / Account
       │
    Business
       │
   ┌───┼────────┐
   │   │        │
 Shop Shop     Shop
   │   │        │
Workers      Workers
```

A single owner may own one or multiple shops.

A business may therefore exist as:

```text
Owner
  │
  └── Business
        │
        └── Main Shop
```

or:

```text
Owner
  │
  └── Business
        ├── Shop A
        ├── Shop B
        └── Shop C
```

The architecture will support both without requiring a separate product.

---

# 7. Ratibu Solo and Ratibu Business

Ratibu will not be implemented as two completely separate applications.

Instead, Ratibu will provide different capabilities based on the size and structure of the business.

## 7.1 Single-Shop / Solo Configuration

Typical structure:

```text
Owner
  │
  └── Business
        │
        └── Shop
```

The owner may manage:

* Bookings
* Clients
* Services
* Payments
* Cash book
* Expenses
* End-of-day reconciliation
* Reports
* Business insights

---

## 7.2 Business / Multi-Shop Configuration

Typical structure:

```text
Owner
   │
   └── Business
          ├── Shop A
          ├── Shop B
          └── Shop C
```

The business configuration will additionally support:

* Multiple shops
* Multiple workers
* Roles and permissions
* Shop-level reporting
* Consolidated business reporting
* Worker performance
* Cross-shop client history
* Cross-shop revenue analysis
* Shop-level cash management

The same underlying product and domain model will support both configurations.

---

# 8. Bookings and Payments

A booking will **not automatically be treated as cash income**.

A booking represents an operational event.

A payment represents a financial event.

The conceptual relationship is:

```text
Client
   ↓
Booking
   ↓
Service
   ↓
Payment
   ↓
Cash Book
```

For example:

```text
Booking
Client: Jane
Service: Hair Styling
Price: KSh 2,500
Status: Completed
Payment: Paid
```

The payment can then generate:

```text
Cash In
Amount: KSh 2,500
Source: Booking
Reference: Booking
Shop: Relevant Shop
```

This distinction allows Ratibu to correctly handle:

* Unpaid bookings
* Partially paid bookings
* Fully paid bookings
* Refunds
* Cancellations
* Other future payment scenarios

---

# 9. Offline Business Activity

Ratibu will support business activity that occurs outside the application.

Examples include:

* Walk-in customers
* Phone bookings
* WhatsApp bookings
* Physical bookings
* Transactions recorded elsewhere
* Other offline sales

These transactions can be entered manually, particularly during end-of-day processing.

For example:

```text
Offline revenue
KSh 18,500
```

This will be represented as a financial event in the relevant shop's cash book.

Where useful, the merchant may provide additional detail such as:

```text
Haircuts       KSh 5,000
Braiding       KSh 8,000
Other          KSh 5,500
-------------------------
Total         KSh 18,500
```

The amount of detail required will be determined during MVP design.

---

# 10. End-of-Day Processing

End-of-day processing will be an important Ratibu workflow.

The conceptual process is:

```text
Ratibu Sales
      +
Offline Sales
      +
Other Cash In
      -
Expenses / Cash Out
      ↓
Expected Cash
      ↓
Actual Cash
      ↓
Reconciliation
```

For example:

```text
Opening cash              KSh 20,000
Booking payments          KSh 35,000
Offline sales             KSh 10,000
Expenses                   KSh  8,000
------------------------------------
Expected closing cash     KSh 57,000

Actual closing cash       KSh 55,500
Difference                KSh -1,500
```

The system should allow the owner or authorized user to record and investigate discrepancies.

This provides a bridge between Ratibu's digital records and the actual physical cash held by the business.

---

# 11. Shop as the Financial Boundary

Every operational and financial event should ultimately be associated with a shop.

Examples:

```text
Booking       → Shop
Payment       → Shop
Expense       → Shop
Cash Entry    → Shop
Worker Activity → Shop
```

This allows Ratibu to calculate both:

```text
Shop-level information
```

and:

```text
Business-level information
```

by aggregating information from individual shops.

For example:

```text
Business
   │
   ├── Westlands
   │      Revenue: KSh 124,000
   │
   ├── Kilimani
   │      Revenue: KSh 96,500
   │
   └── CBD
          Revenue: KSh 64,000
```

The owner can then view the consolidated business position.

---

# 12. Clients

Clients will primarily be associated with the business rather than being treated as completely independent customers at each shop.

A client may interact with multiple shops belonging to the same business.

For example:

```text
Business
   │
   └── Client: Jane
          │
          ├── Westlands booking
          ├── Kilimani booking
          └── Westlands booking
```

This allows Ratibu to provide business-wide client insights such as:

* Total visits
* Total spending
* Most visited shop
* Most purchased services
* Last visit
* Client retention
* Client value

---

# 13. Workers

Workers will belong to the business and may be associated with one or more shops.

Conceptually:

```text
Business
   │
   ├── Worker A
   │      ├── Shop A
   │      └── Shop B
   │
   ├── Worker B
   │      └── Shop C
   │
   └── Worker C
          └── Shop A
```

This allows Ratibu to support businesses where workers may occasionally operate at different branches.

It also allows future reporting such as:

* Revenue generated by worker
* Number of bookings handled
* Average transaction value
* Worker performance
* Performance across multiple shops

---

# 14. Roles and Permissions

Ratibu will support role-based access as the product grows.

Potential roles include:

### Owner

Full access to the business.

### Manager

Access to operations and management for assigned shops.

### Worker

Access to relevant bookings, clients, and assigned work.

### Cashier

Potentially responsible for recording or managing financial transactions.

The exact roles and permissions will be defined during the actor and use-case design phase.

The MVP does not need to implement every possible role.

---

# 15. Business Insights

Business insights will be treated as a first-class product capability rather than a feature added after the main application is complete.

Insights will be divided into levels.

### Level 1 — Descriptive

What happened?

> Revenue this month: KSh 284,500.

### Level 2 — Comparative

How did it change?

> Revenue increased 14% compared with last month.

### Level 3 — Diagnostic

What changed or may explain the result?

> Revenue increased, but average transaction value decreased by 6%.

### Level 4 — Actionable

What could the owner consider doing?

> Saturdays are consistently near capacity. Consider increasing Saturday staffing.

Ratibu should prioritize explainable insights based on the business's actual data.

---

# 16. Ratibu Insights

The application may present important observations directly to the owner.

Examples:

> **Ratibu noticed:** Your revenue increased 18% this month, but average revenue per booking decreased 6%.

> **Ratibu noticed:** Kilimani has fewer bookings than Westlands but generates more revenue per booking.

> **Ratibu noticed:** 14 clients who normally return every 3–4 weeks have not returned in over 6 weeks.

> **Ratibu noticed:** One service accounts for a significant portion of this month's revenue growth.

Insights should be traceable to underlying business data rather than being unexplained recommendations.

---

# 17. Offline-First

Ratibu will be designed with an offline-first approach.

Core business functionality should remain available without an internet connection, including:

* Bookings
* Clients
* Services
* Payments
* Cash-book entries
* Expenses
* End-of-day processing
* Basic reports and insights

The conceptual architecture will be:

```text
              RATIBU
                 │
        ┌────────┴────────┐
        │                 │
 Local Database       Cloud / Backend
        │                 │
        └────── Sync ─────┘
```

Data created while offline should be synchronized when connectivity becomes available.

The synchronization architecture will be designed separately.

---

# 18. MVP Scope

The initial MVP should focus on the core Ratibu loop.

### Business

* Account
* Business
* Shop
* Basic multi-shop support

### People

* Owner
* Workers
* Clients

### Services

* Service catalog
* Service prices

### Bookings

* Create booking
* Reschedule booking
* Cancel booking
* Complete booking

### Money

* Payments
* Cash-in
* Cash-out
* Expenses
* Offline/EOD income
* Cash reconciliation

### Dashboard

* Revenue
* Bookings
* Expenses
* Cash position

### Insights

* Basic trends
* Basic comparisons
* Top services
* Basic worker performance
* Basic client insights
* Basic shop comparisons

Advanced capabilities such as AI-based recommendations, forecasting, payroll, inventory, advanced accounting, marketing automation, and external integrations will not be required for the initial MVP.

---

# 19. Product Boundaries

Ratibu will intentionally avoid becoming a complete accounting or ERP system during the initial development.

The focus will remain:

```text
Operations
    ↓
Money
    ↓
Insights
```

The product should answer:

> **How is my business doing?**

> **Where is my money coming from?**

> **Where is my money going?**

> **What has changed?**

> **Which parts of my business are performing well?**

> **What should I pay attention to?**

Ratibu is not initially intended to replace professional accounting software.

---

# 20. Guiding Architectural Principle

The system should be designed around the principle:

> **Every business event should produce reliable data that can contribute to future business intelligence.**

For example:

```text
Booking
   ↓
Service
   ↓
Payment
   ↓
Cash Book
   ↓
Business Data
   ↓
Insight
```

Similarly:

```text
Offline Activity
   ↓
EOD Entry
   ↓
Cash Book
   ↓
Business Data
   ↓
Insight
```

This means domain and data-model decisions should consider not only immediate functionality but also what useful business questions Ratibu may eventually answer.

---

# 21. Future Direction

The architecture should leave room for future capabilities such as:

* Advanced business insights
* AI-assisted business analysis
* Business forecasting
* Advanced reporting
* Worker commissions
* Inventory
* Customer communication
* Automated reminders
* Marketing tools
* Additional business roles
* Advanced permissions
* External integrations
* Cloud synchronization

These capabilities are intentionally outside the initial MVP.

---

# 22. Consequences

### Positive consequences

* Ratibu can support both small and growing businesses.
* A business can grow from one shop to multiple shops without changing products.
* Booking and financial data can reinforce each other.
* Offline transactions can be included in financial reporting.
* Shop-level and consolidated business reporting become possible.
* Business insights can be generated from real operational and financial data.
* The system remains focused on practical small-business needs.
* Offline-first operation improves reliability in environments with inconsistent connectivity.

### Negative consequences

* The domain model becomes more complex than a simple booking application.
* Multi-shop support introduces additional concepts and permissions.
* Payments and bookings must be modeled separately.
* Cash reconciliation introduces additional financial rules.
* Offline-first synchronization will introduce future technical complexity.
* Business insights require consistent and high-quality underlying data.
* The product has a larger scope than the original Ratibu application.

These costs are considered acceptable because they support the long-term product vision.

---

# 23. Next Decisions

Before implementation begins, the following areas must be specified:

1. **Actors and roles**
2. **Business, shop, and worker relationships**
3. **Detailed use cases**
4. **Booking lifecycle**
5. **Payment lifecycle**
6. **Cash-book rules**
7. **End-of-day workflow**
8. **Multi-shop behavior**
9. **Permissions**
10. **Business-insight definitions**
11. **MVP boundaries**
12. **Offline synchronization requirements**
13. **Domain model**
14. **Data model**
15. **Android architecture**

Implementation in Java should begin only after these decisions have been sufficiently defined.

---

## 24. Summary

Ratibu will evolve from a booking-focused application into an **offline-first business management and intelligence platform**.

Its core relationship is:

```text
                RATIBU
                   │
       ┌───────────┼───────────┐
       │           │           │
   OPERATIONS     MONEY    INTELLIGENCE
       │           │           │
   Bookings     Cash Book    Insights
   Clients      Payments     Trends
   Workers      Expenses     Comparisons
   Services     EOD          Recommendations
   Shops        Reconciliation
       │           │           │
       └───────────┼───────────┘
                   ↓
             BUSINESS OWNER
                   ↓
             Better decisions
```

The product will use a shared core architecture for both single-shop and multi-shop businesses, with capabilities expanding as the business grows.

The central product principle is:

> **Ratibu should not merely tell a business owner what was recorded. It should help them understand what the recorded activity means for their business.**
