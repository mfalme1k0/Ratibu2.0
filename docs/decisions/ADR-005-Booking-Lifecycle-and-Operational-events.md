# ADR-005: Booking Lifecycle and Operational Events

**Status:** Proposed
**Date:** 2026-09-01
**Decision Type:** Product / Architecture
**Project:** Ratibu

---

## 1. Context

Ratibu is a business management application that combines operational management with financial record keeping and business insights.

Bookings are one of the primary operational activities in Ratibu. However, a booking does not necessarily mean that money has been received.

For example:

* A client may book and later cancel.
* A client may book but fail to show up.
* A client may receive a service but pay later.
* A client may pay only part of the amount.
* A client may walk into the shop without using Ratibu.
* A business may record such offline sales at the end of the day.

Therefore, Ratibu must clearly distinguish between:

1. A **booking**
2. A **completed service**
3. A **payment**
4. A **cash-book transaction**

This distinction is necessary to prevent Ratibu from reporting money as received when it has not actually been received.

---

# 2. Problem

If Ratibu automatically treats every booking as income, its financial information may become inaccurate.

For example:

```text
10 bookings × KSh 500
= KSh 5,000
```

does not necessarily mean:

```text
KSh 5,000 received
```

Some bookings may be:

* Cancelled
* No-shows
* Unpaid
* Partially paid
* Paid later

Ratibu therefore needs a booking lifecycle that is independent from the payment lifecycle.

---

# 3. Decision

Ratibu will treat a **Booking**, **Service Completion**, and **Payment** as separate concepts.

The fundamental relationship is:

```text
BOOKING
   ↓
SERVICE
   ↓
PAYMENT
   ↓
CASH BOOK
```

However, these events are not automatically equivalent.

A booking does **not** automatically create a cash-book entry.

A payment creates a financial event when money is actually received.

---

# 4. Booking

A Booking represents an agreement or scheduled intention for a client to receive a service.

A booking will associate:

* Client
* Shop
* Service
* Worker where applicable
* Scheduled date/time
* Expected service duration
* Expected price
* Booking status

Conceptually:

```text
Booking
 ├── Client
 ├── Shop
 ├── Service
 ├── Worker
 ├── Scheduled At
 ├── Duration
 └── Expected Price
```

---

# 5. Booking and Shop

Every booking belongs to exactly one Shop.

For example:

```text
Beauty Empire
   │
   └── Westlands
          │
          └── Booking
```

This allows Ratibu to determine where the service is scheduled to occur.

It also ensures that the eventual financial activity can be associated with the correct shop.

---

# 6. Booking and Client

Every booking should be associated with a Client.

The client belongs to the Business, while the booking records the Shop where the interaction occurs.

For example:

```text
Client: Jane
Business: Beauty Empire

Booking:
    Shop: Westlands
    Service: Haircut
```

Jane can therefore have future bookings at another shop without creating another client record.

---

# 7. Booking and Service

Every booking represents a particular service.

For example:

```text
Booking
   │
   └── Haircut
```

The booking should preserve the service context applicable when it was created.

If the service is later renamed, disabled, or reconfigured, historical bookings should remain understandable.

---

# 8. Booking Price

The booking should capture the price expected at the time of booking.

For example:

```text
Haircut
Current price: KSh 600

Existing booking:
Price: KSh 500
```

If the business later changes the service price from KSh 500 to KSh 600, the existing booking should not silently change to KSh 600.

This preserves historical accuracy.

---

# 9. Booking and Worker

A booking may specify the Worker responsible for performing the service.

For example:

```text
Booking
 ├── Client: Jane
 ├── Service: Haircut
 ├── Shop: Westlands
 └── Worker: Mary
```

The worker must be assigned to the relevant Shop and should be capable of performing the selected Service.

Worker availability will also be considered when creating and scheduling bookings.

---

# 10. Booking Lifecycle

Ratibu will use an explicit booking lifecycle.

The initial lifecycle is:

```text
SCHEDULED
    │
    ├──────────────→ CANCELLED
    │
    ├──────────────→ NO_SHOW
    │
    ↓
IN_PROGRESS
    │
    ↓
COMPLETED
```

The exact transitions will be enforced by domain rules.

---

# 11. SCHEDULED

A booking begins in the:

```text
SCHEDULED
```

state.

This means the booking exists and the service is expected to occur at the scheduled time.

At this stage:

```text
Income received = 0
```

unless a payment has separately been recorded.

---

# 12. CANCELLED

A booking may be cancelled before service completion.

For example:

```text
SCHEDULED
    ↓
CANCELLED
```

A cancelled booking does not automatically generate income.

If a deposit or other payment had already been received, that payment remains a separate financial event and must be handled according to the business's payment/refund rules.

Those refund rules will be defined in the financial ADR.

---

# 13. NO-SHOW

A booking may become:

```text
NO_SHOW
```

when the client fails to attend without completing the service.

A no-show does not automatically create income.

Any financial consequences, such as a retained deposit or cancellation fee, will be explicitly recorded as financial events rather than being inferred solely from the booking status.

---

# 14. IN_PROGRESS

A booking may enter:

```text
IN_PROGRESS
```

when the client has arrived and the service is actively being performed.

This state is useful for businesses where workers need to manage their current workload.

It also creates a clear distinction between:

```text
Client has booked
```

and:

```text
Client is actually being served
```

---

# 15. COMPLETED

A booking becomes:

```text
COMPLETED
```

when the service has actually been performed.

Completion means:

> The business has delivered the service.

It does **not** necessarily mean:

> The business has received payment.

Therefore:

```text
COMPLETED
    ≠
PAID
```

This is a fundamental Ratibu rule.

---

# 16. Payment Is a Separate Event

A completed booking does not automatically create a payment.

For example:

```text
Booking
   ↓
COMPLETED
   ↓
Amount due: KSh 1,000
   ↓
Payment: KSh 0
```

Ratibu should understand that:

```text
Service delivered = Yes
Money received = No
```

This allows Ratibu to support unpaid or credit transactions without corrupting cash information.

---

# 17. Payment States

A completed service may have:

```text
UNPAID
PARTIALLY_PAID
PAID
```

For example:

```text
Service price: KSh 1,000

Payment:
KSh 400

Remaining:
KSh 600
```

The booking remains financially outstanding until the full amount is paid, while the KSh 400 payment is recorded independently.

---

# 18. Multiple Payments

A booking may receive multiple payments.

For example:

```text
Booking
Amount Due: KSh 1,500

Payment 1 → KSh 500
Payment 2 → KSh 500
Payment 3 → KSh 500

Total Paid → KSh 1,500
```

This allows Ratibu to represent real-world payment behavior without modifying the original booking.

---

# 19. Payment Methods

Ratibu should support multiple payment methods.

The initial design should allow methods such as:

* Cash
* Mobile money
* Bank transfer
* Card
* Other

The exact supported payment methods can evolve based on the market and integrations implemented.

The payment method belongs to the Payment event rather than the Booking.

For example:

```text
Booking
   │
   ├── Payment → Cash
   └── Payment → Mobile Money
```

---

# 20. Payment and Cash Book

A payment represents money received by the business.

Therefore, once a payment is confirmed, it can generate a corresponding cash-book transaction.

Conceptually:

```text
PAYMENT
   ↓
CASH BOOK ENTRY
```

The cash-book entry must reference the underlying financial event so that Ratibu can explain where the money came from.

For example:

```text
Cash Book

01 Sep
Haircut
Jane
KSh 500
Cash
```

---

# 21. No Automatic Income from Booking Creation

Creating a booking must never create income.

For example:

```text
Booking created
Amount: KSh 500

Cash Book:
KSh 0
```

The system only records money once a payment is actually recorded.

This prevents future or cancelled bookings from inflating financial reports.

---

# 22. Completed but Unpaid Services

Ratibu must support:

```text
Booking → COMPLETED
Payment → UNPAID
```

For example:

```text
Service delivered:
KSh 2,000

Paid:
KSh 0

Outstanding:
KSh 2,000
```

This is important because the business may allow customers to pay later.

Ratibu can therefore distinguish between:

### Operational revenue/service activity

and:

### Actual cash received.

This distinction will be important for future business insights.

---

# 23. Offline / Walk-In Sales

Not every customer interaction will originate from a Ratibu booking.

For example:

```text
Customer walks into shop
       ↓
Receives haircut
       ↓
Pays KSh 500
       ↓
No Ratibu booking existed
```

Ratibu must support recording this as an **offline sale/payment event**.

The owner or authorized worker can enter it directly.

Conceptually:

```text
OFFLINE SALE
     ↓
PAYMENT
     ↓
CASH BOOK
```

The offline transaction may optionally be associated with:

* Client
* Worker
* Service
* Shop

but these associations should not be required if the business does not have enough information.

---

# 24. End-of-Day Offline Recording

Ratibu will support recording offline activity at the end of the day.

For example, a worker may report:

```text
Offline services:
10 Haircuts
5 Beard trims

Cash received:
KSh 6,500
```

The system can record this as an offline financial event.

This allows businesses that do not record every walk-in transaction in real time to still maintain a useful cash book.

Detailed EOD reconciliation rules will be established in the Cash Book ADR.

---

# 25. Booking vs Offline Sale

Ratibu will distinguish between:

### App-originated activity

```text
Booking
   ↓
Service
   ↓
Payment
```

and:

### Offline activity

```text
Offline Sale
   ↓
Payment
```

Both ultimately contribute to financial reporting.

This means business insights can combine:

```text
Online/App Activity
+
Offline Activity
=
Total Business Activity
```

---

# 26. Booking Rescheduling

A scheduled booking may be rescheduled.

For example:

```text
01 Sep 10:00
      ↓
02 Sep 14:00
```

Rescheduling changes the booking schedule but does not create a financial event.

If a payment already exists, it remains associated with the booking unless the financial rules require otherwise.

---

# 27. Worker Changes

A booking may need to be reassigned to another worker.

For example:

```text
Booking
Worker: Mary

        ↓ reassigned

Worker: John
```

Historical service/payment records should preserve the worker responsible for the completed service.

The booking's current assignment may change before completion.

Once the service is completed, the relevant worker should be preserved as part of the historical service context.

---

# 28. Shop Changes

A booking should not casually move between Shops once financial or service activity has occurred.

Before completion, a business may reschedule a booking to another shop subject to business rules.

After service completion or payment, historical financial records must retain the Shop where the activity actually occurred.

This prevents revenue from being incorrectly attributed to another branch.

---

# 29. Historical Integrity

Once a booking has generated completed service or financial activity, historical information must not be silently rewritten.

Historical records should preserve:

* Client
* Shop
* Worker
* Service
* Price
* Payment amount
* Payment method
* Relevant timestamps

This is essential for accurate reporting and business insights.

---

# 30. Relationship to Business Insights

The booking lifecycle provides the raw operational data required for Ratibu's insights.

For example:

```text
Bookings
   ↓
Completed Services
   ↓
Payments
   ↓
Financial Data
   ↓
Insights
```

Ratibu can then calculate:

* Booking volume
* Completion rate
* Cancellation rate
* No-show rate
* Services performed
* Worker activity
* Revenue
* Outstanding amounts
* Payment method distribution
* Shop performance
* Client spending

Because payments are separate from bookings, Ratibu can produce more accurate financial insights.

---

# 31. Core Domain Rules

The following rules are established:

### Rule 1

> Every booking belongs to exactly one Shop.

### Rule 2

> Every booking is associated with a Client and Service.

### Rule 3

> A booking may optionally identify a Worker where the business model requires one.

### Rule 4

> A booking begins in the SCHEDULED state.

### Rule 5

> A booking may be cancelled before completion.

### Rule 6

> A booking may become NO_SHOW when the client fails to attend.

### Rule 7

> A booking may enter IN_PROGRESS when service begins.

### Rule 8

> A booking becomes COMPLETED only after the service has been delivered.

### Rule 9

> COMPLETED does not imply PAID.

### Rule 10

> Creating a booking does not create a financial transaction.

### Rule 11

> Completing a booking does not automatically create a cash-book transaction.

### Rule 12

> Actual payments are recorded as separate financial events.

### Rule 13

> A booking may have multiple payments.

### Rule 14

> Payments may be partial.

### Rule 15

> Offline sales must be supported independently of bookings.

### Rule 16

> Historical service and financial information must remain stable even when current configuration changes.

### Rule 17

> Business and Shop financial totals are derived from actual financial events rather than booking counts.

---

# 32. Resulting Model

The resulting conceptual model is:

```text
                         BUSINESS
                            │
                           SHOP
                            │
                         BOOKING
                            │
             ┌──────────────┼──────────────┐
             │              │              │
           CLIENT        SERVICE        WORKER
                            │
                            ↓
                       COMPLETED
                            │
                            ↓
                    PAYMENT(S)
                            │
                            ↓
                       CASH BOOK
                            │
                            ↓
                    BUSINESS INSIGHTS
```

Offline activity follows:

```text
                  OFFLINE SALE
                       │
                       ↓
                    PAYMENT
                       │
                       ↓
                   CASH BOOK
                       │
                       ↓
                BUSINESS INSIGHTS
```

---

# 33. Consequences

## Positive consequences

* Prevents bookings from being incorrectly treated as cash received.
* Supports unpaid and partially paid services.
* Supports multiple payments per booking.
* Supports walk-in/offline sales.
* Allows EOD recording of activity that was not entered through bookings.
* Creates accurate cash-book records.
* Preserves historical financial information.
* Enables more meaningful business insights.
* Separates operational state from financial state.
* Supports future payment integrations.

## Negative consequences

* The domain model is more complex than simply marking a booking as "paid."
* Payment tracking requires additional entities and business rules.
* Outstanding balances must be calculated and maintained correctly.
* Offline sales require additional data-entry workflows.
* Refunds and payment reversals require additional financial rules.
* EOD reconciliation introduces another operational workflow.

These costs are accepted because financial accuracy is fundamental to Ratibu's purpose as a business management and insight tool.

---

# 34. Next Decision

The next ADR will define the **Payment, Cash Book and End-of-Day Reconciliation Model**.

It will answer:

* What exactly constitutes a financial transaction?
* What is the difference between income and cash movement?
* How are expenses recorded?
* How are offline sales recorded?
* How are refunds handled?
* How are partial payments represented?
* How does the Cash Book work?
* What happens during EOD?
* How does Ratibu identify discrepancies?
* Can workers submit EOD figures?
* Who can approve/close an EOD?
* How does Ratibu calculate the expected cash balance?
* How does the system distinguish expected cash from actual cash?
* How should the owner see financial information across multiple shops?

This ADR will establish the financial foundation on which Ratibu's business insights will be built.
