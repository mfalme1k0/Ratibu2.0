# ADR-011: Cash Book and Financial Transaction Model

**Status:** Proposed
**Date:** 2026-09-01
**Decision Type:** Domain / Architecture
**Project:** Ratibu

---

# 1. Context

Ratibu is intended to provide small-business owners with both operational and financial visibility.

The system records business activity such as:

* Bookings
* Completed services
* Payments
* Offline sales
* Expenses
* Cash-book transactions
* EOD reconciliation

The financial model must support both:

```text id="7z5n2c"
Businesses that operate primarily through bookings
```

and:

```text id="5v8r1m"
Businesses where significant activity happens offline
```

It must also support:

* Solo owners
* Businesses with workers
* Multiple Shops
* Multiple devices
* Offline operation
* Partial payments
* Credit
* Cash
* Mobile money
* Card payments
* Refunds
* Corrections
* EOD reconciliation

---

# 2. Problem

A simplistic financial model could treat:

```text id="m6k4x1"
Booking = Revenue = Payment = Cash
```

This is incorrect.

For example:

```text id="f8p2q7"
Service price:
KSh 2,000

Customer pays:
KSh 1,000

Outstanding:
KSh 1,000
```

The business has:

```text id="z4h7n2"
Service value = KSh 2,000

Money received = KSh 1,000

Outstanding = KSh 1,000
```

The financial system must preserve these distinctions.

---

# 3. Decision

Ratibu will distinguish between:

1. **Service Value**
2. **Payment**
3. **Expense**
4. **Cash Movement**
5. **Cash Position**
6. **Outstanding Amount**
7. **EOD Reconciliation**

These concepts may be related but must not be treated as identical.

---

# 4. Service Value

Service Value represents the value associated with a completed service or sale.

For example:

```text id="y2c8m4"
Haircut
Price:
KSh 1,000
```

The Service Value is:

```text id="6t7q3p"
KSh 1,000
```

This does not mean the business has received KSh 1,000.

---

# 5. Payment

A Payment represents money actually received from a client.

For example:

```text id="r3m8q1"
Service Value:
KSh 1,000

Payment:
KSh 600

Outstanding:
KSh 400
```

A client may make multiple payments.

```text id="8q2n5m"
Payment 1:
KSh 300

Payment 2:
KSh 300

Payment 3:
KSh 400
```

The service is fully paid once:

```text id="h6k4p8"
Total Payments >= Amount Due
```

subject to the financial rules defined by Ratibu.

---

# 6. Partial Payments

Ratibu will support partial payments.

For example:

```text id="c4n7x2"
Amount Due:
KSh 2,000

Payment:
KSh 1,000

Outstanding:
KSh 1,000
```

The payment record must preserve the actual amount received.

---

# 7. Multiple Payments

A service may have multiple associated payments.

```text id="p7m2k5"
Service
KSh 2,000
   │
   ├── Payment KSh 500
   ├── Payment KSh 500
   └── Payment KSh 1,000
```

This is preferable to modifying the original payment each time.

---

# 8. Overpayment

Ratibu must explicitly define how overpayments are handled.

For example:

```text id="n4q8m2"
Amount Due:
KSh 1,000

Payment:
KSh 1,200
```

The additional KSh 200 should not silently disappear.

The system must either:

* Record an outstanding client credit, or
* Reject the payment if overpayment is not allowed.

The initial implementation should favor explicit handling rather than silent adjustment.

---

# 9. Payment Methods

Ratibu will support multiple payment methods.

Initial methods include:

```text id="k8m3p1"
Cash
Mobile Money
Card
Credit / Outstanding
```

The exact payment-method enumeration will be defined during implementation.

---

# 10. Cash Payment

A cash payment affects physical cash.

For example:

```text id="g7p2c5"
Client pays:
KSh 500 Cash
```

This produces:

```text id="f2m8q4"
Cash In:
KSh 500
```

and contributes to expected cash.

---

# 11. Mobile Money Payment

A mobile-money payment represents money received through a digital payment channel.

For example:

```text id="r5k9m3"
Client pays:
KSh 1,000

Method:
Mobile Money
```

It contributes to:

```text id="z3q6p8"
Payments Received
```

but does not necessarily increase:

```text id="w7m2c4"
Physical Cash
```

---

# 12. Card Payment

Card payments are treated similarly.

They contribute to:

```text id="a4k8n6"
Payments Received
```

but are not automatically treated as physical cash.

---

# 13. Payment Channel vs Cash

Ratibu will distinguish:

```text id="e6m3q8"
Money Received
```

from:

```text id="t4p7n2"
Physical Cash
```

Therefore:

```text id="k5m8c2"
Total Payments Received
```

may differ from:

```text id="n7q3p9"
Cash Held
```

This distinction is fundamental to the financial model.

---

# 14. Credit / Outstanding

A client may receive a service without paying the full amount.

For example:

```text id="w3k8m6"
Service:
KSh 2,000

Paid:
KSh 500

Outstanding:
KSh 1,500
```

The outstanding amount remains associated with the financial obligation.

It is not treated as money received.

---

# 15. Outstanding Payment

Ratibu should make outstanding balances visible.

For example:

```text id="m8p3r6"
Jane Doe

Amount Due:
KSh 2,000

Paid:
KSh 500

Outstanding:
KSh 1,500
```

The client can later make another payment.

---

# 16. Offline Payments

Offline payments are first-class financial transactions.

A user should be able to record:

```text id="v6n2q8"
Offline Sale

Service:
Haircut

Amount:
KSh 700

Payment Method:
Cash
```

without requiring an internet connection.

The transaction is stored locally and synchronized later.

---

# 17. Booking Payment Integration

When a client pays through a booking workflow, Ratibu should automatically associate the payment with the relevant booking/service.

Conceptually:

```text id="r8m4k2"
Booking
   ↓
Service Completed
   ↓
Payment
   ↓
Cash / Payment Channel
```

This reduces duplicate manual entry.

---

# 18. Automatic Financial Recording

Where the booking workflow provides sufficient information, Ratibu should automatically create the appropriate financial record.

For example:

```text id="q5n7m3"
Booking:
Haircut
KSh 1,000

Client pays:
KSh 1,000 cash

        ↓

Payment:
KSh 1,000
Method:
Cash
```

The user should not have to enter the same payment twice.

---

# 19. Manual Financial Recording

Not every transaction originates from a booking.

Ratibu therefore supports direct financial transactions.

For example:

```text id="c7m2p9"
Walk-in Client
   ↓
Service
   ↓
Payment
```

without requiring a prior booking.

This supports real-world business activity.

---

# 20. Offline Walk-In

A worker or owner may record a walk-in completely offline.

```text id="x4n8q2"
Walk-in
   ↓
Select / Create Client
   ↓
Select Service
   ↓
Record Payment
   ↓
Complete Transaction
```

The transaction is saved locally.

---

# 21. Expenses

Ratibu will record business expenses separately from client payments.

For example:

```text id="p3k7m1"
Expense

Category:
Supplies

Amount:
KSh 2,000

Payment Method:
Cash
```

This represents:

```text id="b8q4n6"
Cash Out:
KSh 2,000
```

---

# 22. Expense Categories

Initial expense categories may include:

* Supplies
* Rent
* Utilities
* Transport
* Salaries/Wages
* Maintenance
* Marketing
* Other

The category system should remain extensible.

---

# 23. Cash In

Cash In represents physical cash entering the Shop's cash position.

Sources may include:

* Cash client payments
* Cash sales
* Owner cash injection
* Other approved cash receipts

The exact classification of each source must remain identifiable.

---

# 24. Cash Out

Cash Out represents physical cash leaving the Shop's cash position.

Sources may include:

* Cash expenses
* Owner withdrawals
* Cash refunds
* Other approved withdrawals

Again, the reason for the transaction must remain identifiable.

---

# 25. Owner Cash Injection

A business owner may add personal cash to the business.

For example:

```text id="h5m8q2"
Owner adds:
KSh 10,000

Reason:
Opening working cash
```

This should not be counted as:

```text id="j7p3n6"
Business Revenue
```

It is a cash movement with a different financial meaning.

---

# 26. Owner Withdrawal

Similarly:

```text id="n4m7c8"
Owner withdraws:
KSh 5,000
```

should affect cash position but should not automatically be classified as a business expense.

---

# 27. Refunds

Refunds must be represented explicitly.

For example:

```text id="k8q2m5"
Original Payment:
KSh 1,000

Refund:
KSh 1,000
```

A refund should not simply delete the original payment.

This preserves financial history.

---

# 28. Partial Refund

Partial refunds should be supported.

For example:

```text id="w6p3n8"
Original Payment:
KSh 1,000

Refund:
KSh 300

Net Received:
KSh 700
```

The original payment remains intact.

---

# 29. Corrections

Financial records should generally not be edited destructively.

Instead:

```text id="r7m4q2"
Original Transaction
        ↓
Correction / Reversal
        ↓
Corrected Financial Position
```

This creates a traceable history.

---

# 30. Cash Book

The Cash Book represents the chronological record of relevant cash movements.

For example:

```text id="x8k3m5"
DATE       DESCRIPTION        IN       OUT      BALANCE

01 Sep     Opening Balance     2,000             2,000
01 Sep     Haircut             1,000             3,000
01 Sep     Supplies                      500    2,500
01 Sep     Haircut               800             3,300
```

The exact UI is a later design decision.

---

# 31. Expected Cash

Expected Cash is calculated from known cash movements.

Conceptually:

```text id="m5q8p2"
Opening Cash
+
Cash In
-
Cash Out
=
Expected Cash
```

For example:

```text id="q3n7k6"
Opening:
KSh 2,000

Cash In:
KSh 5,000

Cash Out:
KSh 1,000

Expected:
KSh 6,000
```

---

# 32. Actual Cash

Actual Cash is the physical amount counted at the Shop.

For example:

```text id="v8m2c5"
Expected:
KSh 6,000

Actual:
KSh 5,800
```

Difference:

```text id="p4n7q3"
-KSh 200
```

---

# 33. EOD Reconciliation

EOD compares:

```text id="k6m3r8"
Expected Cash
        vs
Actual Cash
```

The result is:

```text id="x2q7n4"
Difference =
Actual Cash - Expected Cash
```

A difference of zero indicates reconciliation.

---

# 34. Cash Discrepancy

A discrepancy does not automatically indicate wrongdoing.

Possible explanations include:

* Recording error
* Missing transaction
* Incorrect opening balance
* Cash payment not recorded
* Expense not recorded
* Change given incorrectly
* Genuine cash loss

Ratibu should record the discrepancy and allow authorized users to investigate.

---

# 35. EOD Responsibility

EOD may be performed by:

* Owner
* Manager
* Authorized Worker

depending on permissions.

A solo owner can perform EOD without needing another user.

---

# 36. Shop-Level Financial Isolation

Financial transactions belong to a Shop.

For example:

```text id="b7m4p2"
Business
   │
   ├── Shop A
   │    └── Transactions
   │
   └── Shop B
        └── Transactions
```

Transactions must not accidentally move between Shops.

---

# 37. Business-Level Financial Aggregation

Business-level financial views aggregate Shop-level transactions.

For example:

```text id="n5q8m3"
Shop A:
KSh 100,000

Shop B:
KSh 80,000

Shop C:
KSh 70,000

Business:
KSh 250,000
```

The Business total should be derived from the underlying Shop-level data.

---

# 38. Revenue vs Cash Flow

Ratibu will distinguish between:

```text id="c8m3q7"
Revenue / Service Value
```

and:

```text id="p6n2r5"
Cash Flow
```

For example:

```text id="k4m8x2"
Services:
KSh 100,000

Payments received:
KSh 70,000

Outstanding:
KSh 30,000
```

The business performed KSh 100,000 worth of services but received KSh 70,000.

This distinction is essential for meaningful business insights.

---

# 39. Payment Aggregation

Payments should be aggregatable by:

* Shop
* Date
* Payment method
* Service
* Worker
* Client
* Booking
* Business

For example:

```text id="t3q7m8"
September Payments

Cash:
KSh 80,000

Mobile Money:
KSh 120,000

Card:
KSh 30,000

Total:
KSh 230,000
```

---

# 40. Financial Transaction Identity

Every financial transaction must have a stable unique identity.

This supports:

* Offline creation
* Synchronization
* Duplicate prevention
* Auditability
* Corrections
* Reconciliation

This is particularly important because transactions may be created on multiple devices.

---

# 41. Financial Transaction Immutability

Once a financial transaction has been synchronized and accepted, the preferred approach is to avoid destructive modification.

Instead of:

```text id="a8m3q7"
Payment:
KSh 1,000

Edit:
KSh 500
```

use:

```text id="x5n7p2"
Payment:
KSh 1,000

Correction:
-KSh 500

Corrected position:
KSh 500
```

This preserves the audit trail.

---

# 42. Transaction Attribution

Each transaction should preserve appropriate context.

Conceptually:

```text id="q7m3k8"
Transaction
   │
   ├── Business
   ├── Shop
   ├── User
   ├── Time
   ├── Amount
   ├── Type
   ├── Payment Method
   └── Related Entity
```

The related entity may be:

* Booking
* Service
* Client
* Expense
* EOD
* Other financial event

---

# 43. Event Time vs Sync Time

Ratibu must distinguish:

```text id="m2q8k5"
Transaction occurred:
14:32
```

from:

```text id="n7p3c4"
Transaction synchronized:
18:47
```

This is especially important for offline transactions.

---

# 44. Financial Audit Trail

Important financial actions should be attributable.

For example:

```text id="r5m8q3"
Payment
KSh 1,000

Recorded by:
Mary

Shop:
Westlands

Occurred:
14:32

Synced:
18:47
```

Corrections should similarly identify who performed them.

---

# 45. Cash Book and Multiple Devices

Multiple workers may record transactions simultaneously.

For example:

```text id="v7n2m5"
Worker A
Cash In KSh 500

Worker B
Cash In KSh 1,000

Owner
Cash Out KSh 300
```

After synchronization:

```text id="q4m8p7"
Expected Cash =
Opening
+ 500
+ 1,000
- 300
```

The system must retain all valid transactions.

---

# 46. No Silent Merging of Financial Transactions

Financial transactions must not be merged merely because they have:

* The same amount
* The same client
* The same timestamp
* The same payment method

Each transaction represents a distinct financial event unless the domain explicitly determines otherwise.

---

# 47. Offline Financial Integrity

Offline mode must not weaken financial integrity.

The local application should enforce:

* Required fields
* Valid amounts
* Valid payment methods
* Valid Shop association
* Valid transaction types
* Relevant domain rules

The server performs authoritative validation during synchronization.

---

# 48. Negative Amounts

Financial amounts should not be represented ambiguously using arbitrary negative numbers entered by users.

For example, users should not have to enter:

```text id="s4m7n2"
-500
```

to mean an expense.

Instead:

```text id="k8q3m6"
Type:
Expense

Amount:
KSh 500
```

The system determines the resulting Cash Out movement.

This reduces user errors.

---

# 49. Currency

Ratibu should support a Business currency.

The initial implementation may focus on a single currency per Business.

The currency should be explicitly associated with the Business rather than hardcoded throughout the application.

Multi-currency support may be introduced later if required.

---

# 50. Decimal Precision

Financial amounts should use a representation appropriate for exact monetary values.

The application should not rely on binary floating-point arithmetic for financial calculations.

The exact implementation strategy will be defined in the technical architecture ADR.

---

# 51. Financial Data and Insights

The financial model feeds the Insight system.

For example:

```text id="f8m3q5"
Payments
   ↓
Revenue Received
   ↓
Insights
```

and:

```text id="n7k2p4"
Expenses
   ↓
Cash Flow
   ↓
Insights
```

while:

```text id="m4q8c6"
Outstanding Payments
   ↓
Receivables Insight
```

---

# 52. Example Complete Transaction

A booking may produce:

```text id="w3m7p9"
Booking
───────────────
Service:
Haircut

Service Value:
KSh 1,000

Payment:
KSh 600

Method:
Cash

Outstanding:
KSh 400
```

The financial result is:

```text id="x8q2m5"
Service Value:
KSh 1,000

Payments Received:
KSh 600

Cash In:
KSh 600

Outstanding:
KSh 400
```

---

# 53. Example Offline Transaction

A walk-in customer arrives while the Shop is offline.

```text id="q7m4n2"
Walk-in
   ↓
Haircut
   ↓
KSh 800
   ↓
Cash
```

Ratibu:

```text id="c5p8m3"
1. Saves transaction locally
2. Updates local Cash Book
3. Updates local Expected Cash
4. Updates local insights
5. Marks transaction Pending Sync
```

When connectivity returns:

```text id="m8q3k7"
Pending
   ↓
Sync
   ↓
Server
   ↓
Confirmed
```

---

# 54. Example Partial Payment

```text id="n5m8p2"
Service:
KSh 2,000

Payment 1:
KSh 500 Cash

Payment 2:
KSh 1,000 Mobile Money

Outstanding:
KSh 500
```

Cash Book receives only:

```text id="r7q3m5"
Cash In:
KSh 500
```

while total Payments Received is:

```text id="x4n8k2"
KSh 1,500
```

This demonstrates why Payment and Cash Position cannot be the same concept.

---

# 55. Example Multi-Shop Business

```text id="v6m3q8"
Business
   │
   ├── Shop A
   │     ├── Payments
   │     ├── Expenses
   │     └── Cash Book
   │
   ├── Shop B
   │     ├── Payments
   │     ├── Expenses
   │     └── Cash Book
   │
   └── Shop C
         ├── Payments
         ├── Expenses
         └── Cash Book
```

Each Shop maintains its own financial activity.

The Business dashboard aggregates the information.

---

# 56. Core Financial Model

The conceptual relationship is:

```text id="q8m3k5"
                    BUSINESS
                       │
                      SHOP
                       │
             ┌─────────┼─────────┐
             │         │         │
         PAYMENTS   EXPENSES   CASH EVENTS
             │         │         │
             └─────────┼─────────┘
                       ↓
                   CASH BOOK
                       │
                       ↓
                EXPECTED CASH
                       │
                       ↓
                 ACTUAL CASH
                       │
                       ↓
                     EOD
                       │
                       ↓
                RECONCILIATION
```

Separately:

```text id="m7q2p8"
SERVICE VALUE
      │
      ├── Payments
      │
      └── Outstanding
```

---

# 57. Core Domain Rules

### Rule 1

> A Booking is not automatically a Payment.

### Rule 2

> A Service Value is not automatically money received.

### Rule 3

> Payments represent actual money received.

### Rule 4

> Partial payments are supported.

### Rule 5

> Multiple payments may satisfy one financial obligation.

### Rule 6

> Cash payments affect physical cash.

### Rule 7

> Mobile Money and Card payments affect received funds but not physical cash.

### Rule 8

> Outstanding amounts are not treated as received money.

### Rule 9

> Offline payments are first-class financial transactions.

### Rule 10

> Walk-in/offline transactions do not require a prior booking.

### Rule 11

> Expenses are distinct from client payments.

### Rule 12

> Owner cash injections are not automatically business revenue.

### Rule 13

> Owner withdrawals are not automatically business expenses.

### Rule 14

> Refunds do not delete the original payment.

### Rule 15

> Financial corrections should preserve the original transaction.

### Rule 16

> Cash Book records relevant physical cash movements.

### Rule 17

> Expected Cash is calculated from known cash movements.

### Rule 18

> Actual Cash represents the physical cash counted.

### Rule 19

> EOD compares Expected Cash with Actual Cash.

### Rule 20

> Financial transactions belong to a Shop.

### Rule 21

> Business-level financial information is derived from Shop-level transactions.

### Rule 22

> Every financial transaction must have a stable unique identity.

### Rule 23

> Financial transactions must remain attributable to the user who created them.

### Rule 24

> Offline financial transactions must remain durable until synchronized or explicitly resolved.

### Rule 25

> Financial calculations must use exact monetary arithmetic.

### Rule 26

> Financial transactions must not be silently merged or duplicated during synchronization.

---

# 58. Consequences

## Positive consequences

* Ratibu can accurately represent real-world business finances.
* Cash and revenue are not incorrectly conflated.
* Partial payments are supported.
* Offline payments are fully supported.
* Multiple payment methods are supported.
* Walk-in transactions are supported.
* Cash Book becomes a meaningful financial record.
* EOD reconciliation has a clear foundation.
* Multi-Shop financial reporting becomes possible.
* Business insights can be based on trustworthy financial data.
* Financial history remains auditable.
* The model supports both solo operators and businesses with workers.

## Negative consequences

* The financial domain is more complex than a simple income/expense ledger.
* Multiple concepts must be modeled separately.
* Financial corrections require additional records.
* Offline synchronization requires careful handling.
* EOD introduces additional state and rules.
* Partial payments and outstanding balances add complexity.
* Multi-Shop aggregation requires careful scoping.
* Financial testing will require substantial coverage.

These costs are accepted because trustworthy financial information is central to Ratibu's value proposition.

---

# 59. Next Decision

The next ADR should define the **Domain Model and Aggregate Boundaries**.

We now have enough product decisions to begin determining the major domain concepts.

That ADR should establish the relationships and ownership boundaries between:

* Business
* Shop
* User
* Worker Profile
* Client
* Service
* Booking
* Payment
* Financial Transaction
* Expense
* Cash Book
* EOD Reconciliation
* Insight

It should specifically answer:

> **Which objects are independent entities, which belong inside aggregates, and where should business rules live?**

This decision will be the bridge between our product ADRs and the eventual Java implementation.
