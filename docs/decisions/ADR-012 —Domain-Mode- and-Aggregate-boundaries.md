ADR-012 — Domain Model and Aggregate Boundaries

Status: Proposed
Date: 2026-09-01
Decision Type: Architecture / Domain Design

1. Context

Ratibu has grown beyond a simple booking application. It now needs to support:

Businesses
Multiple shops per business
Owners
Managers
Workers
Owners who also work as workers
Clients
Services
Bookings
Payments
Offline transactions
Expenses
Cash Book
End-of-day reconciliation
Business insights
Offline-first synchronization

Because these concepts interact heavily, Ratibu needs clearly defined ownership boundaries.

Without explicit boundaries, business rules can become scattered across screens, database code, services, and individual entities. This would make the system harder to extend as a business grows from a single owner-operated shop into a business with multiple shops and many workers.

The primary question is:

Which objects are independent entities, which belong to other aggregates, and where should business rules live?

2. Decision

Ratibu will use a domain-oriented model with explicit aggregate boundaries.

The core hierarchy will be:

BUSINESS
   │
   ├── SHOPS
   │      │
   │      ├── SERVICES
   │      ├── BOOKINGS
   │      ├── PAYMENTS
   │      ├── EXPENSES
   │      ├── CASH EVENTS
   │      └── EOD RECONCILIATIONS
   │
   ├── USERS / ACCESS
   │
   └── BUSINESS-LEVEL INSIGHTS

However, not everything under a Business will be physically embedded inside one enormous Business aggregate.

Instead, Ratibu will use separate aggregates connected through stable IDs/references.

3. Core Domain Objects
3.1 Business

Business represents the business itself.

Examples:

John's Barbershop
Mary's Salon
Fidel Auto Services

A Business may contain multiple Shops.

Responsibilities

The Business is responsible for:

Business identity
Business-level configuration
Currency
Business membership/access
Business ownership
Business-wide settings
Aggregating information from shops

It should not directly contain every booking, payment, or expense.

For example:

Business
 ├── Shop A
 │    ├── Booking
 │    ├── Payment
 │    └── Expense
 │
 └── Shop B
      ├── Booking
      ├── Payment
      └── Expense

This prevents a large business from creating one enormous aggregate.

4. Shop

Shop represents an operational location.

A business may have:

Business
 ├── Shop Nairobi
 ├── Shop Westlands
 └── Shop Karen

A Shop becomes the primary boundary for operational and financial activity.

Shop owns/references:
Services
Bookings
Payments
Expenses
Cash events
Cash Book
EOD reconciliation

Most day-to-day operations therefore happen within a Shop.

Important rule

A financial transaction must belong to a Shop, not merely to a Business.

This allows Ratibu to answer:

"How much cash did the Westlands shop make today?"

rather than only:

"How much did the business make?"

5. User

User represents a person using Ratibu.

A User is an identity, not a job position.

For example:

User: John

John could simultaneously be:

Owner
Worker

This follows ADR-009.

The User should therefore not be duplicated just because the person has multiple responsibilities.

6. Worker Profile

A WorkerProfile represents a user's worker-related information within a business/shop.

It is not another user account.

Conceptually:

User
  │
  ├── Owner role
  │
  └── Worker Profile

This allows Ratibu to attribute work to the correct person.

For example:

John
 ├── Owner
 └── Worker
      └── completed haircut

The same user can therefore appear as the worker responsible for a service while still retaining ownership permissions.

7. Client

Client represents a customer.

A client should be an independent entity rather than being embedded inside a booking.

This is important because the same client may have:

Client
 ├── Booking 1
 ├── Booking 2
 ├── Booking 3
 └── Payment history

This enables future functionality such as:

Client history
Visit frequency
Spending history
Outstanding balances
Customer insights
Returning-client statistics

A client can therefore exist even when they have no current booking.

8. Service

Service represents something a Shop offers.

Examples:

Haircut       KSh 500
Hair Coloring KSh 2,000
Car Wash      KSh 800

Services belong to a Shop because different shops may offer different services and prices.

For example:

Shop A
 ├── Haircut: KSh 500
 └── Shave: KSh 300

Shop B
 ├── Haircut: KSh 700
 └── Shave: KSh 400

The service definition is therefore shop-specific.

9. Booking

Booking represents an appointment/reservation or scheduled service.

A Booking references:

Shop
Client
Service
Worker, when applicable
Scheduled time
Booking status

A booking is an operational entity, not a financial transaction.

This distinction is extremely important.

Booking
      │
      └── Service Value
              │
              ├── Payment 1
              ├── Payment 2
              └── Outstanding

A booking does not automatically mean money was received.

This preserves the decision made in ADR-011.

10. Payment

Payment represents actual money received from a client.

It should be independent from the Booking.

A Payment may reference a Booking, but it does not require one.

Therefore:

Booked customer
Booking
   ↓
Payment
Walk-in customer
Walk-in
   ↓
Payment

This supports offline transactions naturally.

Payment methods include:

CASH
MOBILE_MONEY
CARD

Credit/outstanding should be represented as an unpaid obligation rather than pretending money was received.

11. Financial Transaction

Ratibu will use a broader concept of Financial Transaction to represent monetary events.

Examples:

Payment
Expense
Owner Cash Injection
Owner Withdrawal
Refund
Cash Adjustment

However, these should retain their specific types.

We should not create a generic transaction that simply has:

amount = -500

Instead:

type = EXPENSE
amount = 500

or:

type = PAYMENT
amount = 500

This preserves meaning.

12. Expense

Expense represents money spent by the business.

Examples:

Electricity     KSh 3,000
Supplies        KSh 1,500
Transport       KSh 500

An expense is different from a client payment.

Client → Business
        Payment

Business → Supplier
           Expense

Expenses may affect physical cash when paid in cash.

13. Cash Book

The Cash Book represents the chronological record of physical cash movements.

It should not simply be a copy of every financial transaction.

For example:

Cash Payment              +500
Cash Payment              +1,000
Cash Expense              -300
Owner Withdrawal          -500

But:

Mobile Money Payment      +1,000

does not increase physical cash.

Therefore:

Cash Book is derived from relevant physical cash events, not from every payment.

14. EOD Reconciliation

EOD reconciliation represents the process of comparing:

Expected Cash
      vs
Actual Cash

The expected amount comes from recorded cash movements.

For example:

Opening Cash       5,000
Cash received      8,000
Cash expenses     -2,000
Owner withdrawal  -1,000
------------------------
Expected Cash     10,000

If the physical count is:

Actual Cash = 9,500

then:

Difference = -500

The discrepancy is recorded rather than silently modifying another transaction.

15. Insight

Insight should not become a primary source of financial truth.

Insights are derived from domain data.

For example:

Payments
Expenses
Bookings
Services
Workers
Clients
        ↓
   Analytics
        ↓
     Insights

Examples:

Daily revenue
Cash received
Most popular service
Worker performance
Client frequency
Outstanding balances
Expense trends

This means Ratibu can regenerate insights from underlying data rather than storing analytics as authoritative financial records.

16. Aggregate Boundaries

The proposed aggregate boundaries are:

Business Aggregate
Business

Responsible for business-level rules and configuration.

Shop Aggregate
Shop

Responsible for shop-level configuration and operational state.

Booking Aggregate
Booking

Responsible for booking lifecycle.

For example:

PENDING
CONFIRMED
COMPLETED
CANCELLED

Booking rules should live around the Booking domain rather than inside the UI.

Payment Aggregate
Payment

Responsible for payment-specific rules.

Examples:

Amount must be positive.
Payment method must be valid.
Payment cannot silently change after synchronization.
Refunds must preserve the original payment.
Expense Aggregate
Expense

Responsible for expense-specific rules.

Cash/EOD Aggregate
Cash Events
      ↓
Cash Book
      ↓
EOD Reconciliation

Cash reconciliation needs its own boundary because it represents a financial state that must remain auditable.

17. Relationships

The high-level domain relationship becomes:

                    BUSINESS
                       │
          ┌────────────┴────────────┐
          │                         │
        USERS                      SHOPS
                                    │
             ┌──────────────────────┼──────────────────────┐
             │          │           │          │            │
          SERVICES   BOOKINGS    PAYMENTS   EXPENSES    CASH EVENTS
                        │
                 ┌──────┴──────┐
                 │             │
               CLIENT        WORKER
                               │
                              USER

And financially:

SERVICE VALUE
      │
      ├──────── PAYMENT
      │
      └──────── OUTSTANDING


PAYMENTS + EXPENSES + CASH EVENTS
                 │
                 ↓
             CASH BOOK
                 │
                 ↓
          EXPECTED CASH
                 │
                 ↓
           EOD RECONCILIATION
                 │
                 ↓
             ACTUAL CASH
18. Where Business Rules Live

A major decision in this ADR is:

Business rules belong in the domain/application layer, not in Android screens or database entities alone.

For example, the UI should not decide:

"Can this booking be cancelled?"

Instead, the domain should determine whether cancellation is valid.

Similarly:

"Does this payment affect physical cash?"

should be determined by the financial domain rules.

The Android UI should primarily:

Capture input
      ↓
Invoke use case
      ↓
Display result

rather than implementing the business rules itself.

19. Aggregate Communication

Aggregates should not directly manipulate each other's internal state.

Instead, they communicate through:

IDs
application services/use cases
domain events where appropriate

For example:

Booking
   │
   │ booking completed
   ↓
Application Service
   │
   ↓
Payment / Financial Transaction

Rather than:

Booking
   └── directly modifies Payment internals

This becomes especially important when synchronization is introduced.

20. Offline-First Implications

Because Ratibu is offline-first, aggregates must be independently identifiable.

For example:

Payment ID
Booking ID
Expense ID
EOD ID

must be created locally without requiring the server.

A transaction created offline should therefore already have a stable identity before synchronization.

This also helps prevent duplicate financial records when multiple devices synchronize.

21. Financial Data Must Be Auditable

Financial aggregates should favor:

Original Transaction
        ↓
Correction / Reversal
        ↓
New State

rather than:

Original Transaction
        ↓
DELETE

For example, if a KSh 1,000 payment was recorded incorrectly, Ratibu should preserve the original record and create an appropriate correction/refund.

This supports:

Auditing
Synchronization
Dispute resolution
EOD reconciliation
Financial history
22. Why We Are Not Making Business One Giant Aggregate

An alternative would have been:

Business
 ├── Users
 ├── Shops
 ├── Clients
 ├── Services
 ├── Bookings
 ├── Payments
 ├── Expenses
 └── Cash Book

with everything managed as one aggregate.

We reject this approach.

It would make the Business aggregate extremely large and would become problematic when:

Multiple workers operate simultaneously.
Multiple devices work offline.
Multiple shops exist.
Many transactions accumulate.
Synchronization occurs.

Instead, Ratibu uses smaller aggregates that can operate independently while still belonging to the same Business.

23. Resulting Architecture

The domain structure is therefore:

                    ┌─────────────┐
                    │   BUSINESS  │
                    └──────┬──────┘
                           │
             ┌─────────────┴─────────────┐
             │                           │
       ┌─────▼─────┐               ┌────▼─────┐
       │   USERS   │               │   SHOPS  │
       └───────────┘               └────┬─────┘
                                       │
        ┌──────────────┬───────────────┼───────────────┐
        │              │               │               │
   ┌────▼────┐    ┌────▼─────┐   ┌────▼─────┐   ┌────▼─────┐
   │ SERVICES│    │ BOOKINGS │   │ PAYMENTS │   │ EXPENSES │
   └─────────┘    └────┬─────┘   └────┬─────┘   └──────────┘
                        │              │
                   ┌────▼────┐        │
                   │ CLIENT  │        │
                   └─────────┘        │
                                      │
                           ┌──────────▼──────────┐
                           │   CASH BOOK / EOD   │
                           └──────────┬──────────┘
                                      │
                                ┌─────▼─────┐
                                │  INSIGHTS │
                                └───────────┘
24. Consequences
Positive
Clear ownership boundaries.
Easier testing.
Easier offline synchronization.
Financial history remains auditable.
Supports multiple shops.
Supports solo owners.
Supports workers and managers.
Allows Ratibu to grow without redesigning its core model.
Business insights remain derived rather than becoming another source of truth.
Domain rules remain independent of Android UI.
Negative
More domain concepts.
More IDs/references between aggregates.
Synchronization becomes more sophisticated.
Some operations require application-level coordination between aggregates.

These are accepted because Ratibu's offline-first and financial requirements make a simplistic model unsafe.

25. Final Decision

Ratibu will use separate domain aggregates with explicit ownership boundaries.

The key principles are:

Business is the organizational boundary.
Shop is the primary operational and financial boundary.
User represents identity, not a job role.
Worker Profile is optional and attached to a User.
Users may hold multiple roles.
Clients are independent entities.
Services belong to Shops.
Bookings represent scheduled/operational activity.
Payments represent actual money received.
Expenses represent business spending.
Financial transactions remain auditable.
Cash Book represents physical cash movements.
EOD Reconciliation compares expected and actual cash.
Insights are derived from domain data.
Aggregates communicate through IDs/use cases rather than directly modifying each other's internals.
All important entities must support offline creation and synchronization.
Business rules belong in the domain/application layer rather than the Android UI.
