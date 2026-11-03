# ADR-006: Payments, Cash Book and End-of-Day Reconciliation

**Status:** Proposed
**Date:** 2026-09-01
**Decision Type:** Product / Architecture
**Project:** Ratibu

---

# 1. Context

Ratibu is intended to provide small business owners with both operational management and meaningful financial insights.

ADR-005 established that:

```text
BOOKING ≠ SERVICE COMPLETION ≠ PAYMENT
```

A booking does not automatically represent money received.

Ratibu must also support businesses where not every transaction originates from the application.

For example, a customer may walk into a shop, receive a service, and pay immediately without ever creating a Ratibu booking.

The business may also have:

* Cash expenses
* Non-cash payments
* Refunds
* Owner cash movements
* Other cash-in and cash-out transactions
* Transactions recorded during the day
* End-of-day reconciliation

Therefore, Ratibu needs a financial model that records individual financial events while also providing an end-of-day process for verifying the shop's actual financial position.

---

# 2. Problem

A system based only on bookings cannot accurately represent the financial reality of a business.

For example:

```text
10 bookings
5 walk-in customers
2 cash expenses
1 mobile-money payment
1 refund
```

The business's actual financial position cannot be derived simply from the number of bookings.

Similarly, recording only one large EOD amount loses important information.

For example:

```text
End of day:
Cash received = KSh 12,500
```

does not tell the owner:

* What services generated the money
* Which worker performed them
* Which transactions were cash
* Which were mobile money
* Which sales were recorded during the day
* Whether the cash balance matches expectations

Ratibu therefore needs **transaction-level financial records**, with EOD functioning as a reconciliation process.

---

# 3. Decision

Ratibu will use a **transaction-based Cash Book**.

Individual financial events will be recorded throughout the day.

End-of-day reconciliation will compare the expected financial position against the actual position reported by the shop.

The fundamental flow is:

```text
FINANCIAL EVENT
      ↓
CASH BOOK
      ↓
DAILY LEDGER
      ↓
END-OF-DAY RECONCILIATION
      ↓
BUSINESS INSIGHTS
```

EOD will not replace transaction recording.

It will validate and close the day's financial activity.

---

# 4. Financial Events

A financial event represents a movement or recognition of money relevant to the business.

Initial financial event categories include:

### Money In

* Booking payment
* Offline sale/payment
* Other income

### Money Out

* Business expense
* Refund
* Other cash-out

Additional categories may be introduced later.

---

# 5. Payment

A Payment represents money received for a service or sale.

A payment may originate from:

```text
Booking
```

or:

```text
Offline Sale
```

For example:

```text
Booking
   ↓
Payment
   ↓
Cash Book
```

or:

```text
Walk-in
   ↓
Offline Sale
   ↓
Payment
   ↓
Cash Book
```

---

# 6. Offline Payments During the Day

Ratibu will support recording offline transactions **at any time during the business day**.

Offline transactions are not restricted to EOD.

For example:

```text
10:15
Walk-in haircut
KSh 500 cash

11:40
Walk-in beard trim
KSh 300 cash

14:20
Walk-in haircut
KSh 500 mobile money
```

Each transaction can be recorded independently.

The Cash Book would therefore contain:

```text
10:15  Haircut       +500  Cash
11:40  Beard Trim    +300  Cash
14:20  Haircut       +500  Mobile Money
```

This provides much better financial visibility than one EOD total.

---

# 7. Offline Sale

An Offline Sale represents a service or sale that was not initiated through a Ratibu booking.

It may optionally reference:

* Client
* Worker
* Service
* Shop

The information should be as flexible as possible because an offline transaction may have incomplete information.

For example:

```text
Offline Sale
├── Shop: Westlands
├── Service: Haircut
├── Worker: Mary
├── Amount: KSh 500
└── Payment Method: Cash
```

But the system should also permit:

```text
Offline Sale
├── Shop: Westlands
├── Amount: KSh 500
└── Payment Method: Cash
```

when the worker does not have enough information to identify the client or service.

---

# 8. Transaction Timing

Financial events should record when the transaction actually occurred.

For example:

```text
Service:
01 Sep 2026 10:15

Payment:
01 Sep 2026 10:17
```

This is preferable to assigning all offline activity to:

```text
01 Sep 2026 23:59
```

during EOD.

Accurate timestamps allow Ratibu to provide useful operational and financial insights.

---

# 9. Cash Book

The Cash Book is the shop's chronological record of financial movements.

Conceptually:

```text
CASH BOOK
────────────────────────────────
Time     Description       Amount
────────────────────────────────
08:00    Opening Balance   +5,000
10:15    Haircut             +500
11:40    Beard Trim          +300
13:00    Supplies            -800
14:20    Haircut             +500
────────────────────────────────
Expected Closing          5,500
```

The Cash Book belongs to the Shop.

Each Shop maintains its own financial ledger.

---

# 10. Payment Methods

Ratibu will distinguish payment methods.

Initial methods may include:

* Cash
* Mobile money
* Bank transfer
* Card
* Other

The payment method determines where or how the money was received.

This becomes important for reconciliation.

For example:

```text
Cash received:       KSh 5,000
Mobile money:        KSh 8,000
Card:                KSh 2,000
```

The owner can therefore understand the composition of daily receipts.

---

# 11. Cash vs Non-Cash Transactions

Ratibu will distinguish between cash and non-cash financial movements.

For example:

```text
Cash payment
     ↓
Physical cash expected to increase

Mobile money payment
     ↓
Mobile-money balance expected to increase

Bank transfer
     ↓
Bank balance expected to increase
```

This distinction is essential for EOD reconciliation.

A mobile-money payment should not be treated as physical cash simply because it represents income.

---

# 12. Cash-Out Transactions

The Cash Book must also support money leaving the business.

Examples include:

* Supplies
* Transport
* Utilities
* Small operational expenses
* Refunds
* Other business cash-outs

For example:

```text
Cash Sale        +500
Supplies         -200
Transport        -100
```

The expected physical cash balance becomes:

```text
Opening Cash
+ Cash Received
- Cash Paid Out
= Expected Cash
```

---

# 13. Expenses

Expenses are financial events representing business costs.

For example:

```text
Expense
├── Shop: Westlands
├── Category: Supplies
├── Amount: KSh 1,200
├── Payment Method: Cash
└── Time: 15:20
```

A cash expense reduces the expected physical cash balance.

A non-cash expense affects financial reporting but does not reduce physical cash.

---

# 14. Refunds

Refunds will be represented as financial events rather than simply modifying the original payment.

For example:

```text
Original Payment
      +500
       ↓
Refund
      -500
```

The original transaction remains historically visible.

This prevents financial records from being rewritten.

Detailed refund rules will be refined as payment behavior is implemented.

---

# 15. Opening Balance

A Shop's daily cash position begins with an opening balance.

Conceptually:

```text
Opening Balance
      +
Cash In
      -
Cash Out
      =
Expected Closing Cash
```

The opening balance should normally correspond to the previous day's reconciled closing balance.

---

# 16. Expected Closing Cash

At any point during the day Ratibu should be able to calculate the expected cash position.

For example:

```text
Opening Cash           KSh 5,000
Cash Sales             +KSh 7,500
Cash Expenses          -KSh 1,200
Cash Refunds           -KSh   500
────────────────────────────────
Expected Cash          KSh 10,800
```

This calculation can be viewed during the day, not only at EOD.

This gives the owner an up-to-date view of expected cash.

---

# 17. End-of-Day Reconciliation

EOD is a reconciliation process.

At the end of the day, an authorized user records the actual closing position.

For physical cash:

```text
Expected Cash: KSh 10,800
Actual Cash:   KSh 10,500

Difference:    -KSh 300
```

Ratibu should identify this as a discrepancy.

---

# 18. EOD Is Not a New Sale

EOD reconciliation must not create another income transaction.

For example, if the Cash Book already contains:

```text
Haircut       +500
Beard Trim    +300
Haircut       +500
```

the EOD process should not create:

```text
EOD Sales     +1,300
```

That would duplicate the financial activity.

Instead, EOD verifies the existing transactions.

---

# 19. EOD Discrepancy

A discrepancy occurs when:

```text
Actual Closing Balance
≠
Expected Closing Balance
```

For example:

```text
Expected: KSh 10,800
Actual:   KSh 10,500
Difference: -KSh 300
```

Ratibu should record the discrepancy rather than silently modifying the Cash Book to force the balances to match.

This is important for financial integrity.

---

# 20. Discrepancy Explanation

The system should allow an authorized user to provide an explanation for a discrepancy.

For example:

```text
Difference:
-KSh 300

Explanation:
"Cash expense for delivery was not recorded."
```

The exact mechanism for correcting the underlying records will be defined during financial workflow implementation.

The important rule is:

> A discrepancy should remain visible until properly resolved.

---

# 21. EOD Responsibility

The user responsible for closing a Shop's day must have appropriate permissions.

For example:

```text
Worker
   ↓
May record transactions

Manager
   ↓
May reconcile / close day

Owner
   ↓
May review and override where permitted
```

The exact permission matrix will follow the access-control model from ADR-002.

---

# 22. Shop-Level EOD

EOD is performed per Shop.

For example:

```text
Westlands
   └── EOD → Closed

Kilimani
   └── EOD → Still Open

CBD
   └── EOD → Closed
```

One shop closing its day must not automatically close every other shop belonging to the Business.

This is necessary for businesses operating multiple branches with different schedules.

---

# 23. Business-Level Financial View

The Business can aggregate financial information from its Shops.

For example:

```text
Westlands
Cash: KSh 10,800

Kilimani
Cash: KSh 8,200

CBD
Cash: KSh 6,500
```

Ratibu can provide:

```text
Business Financial Overview
────────────────────────────
Westlands      10,800
Kilimani        8,200
CBD             6,500
────────────────────────────
Total          25,500
```

These totals are derived from shop-level financial events.

---

# 24. Cash Book and Business Insights

The Cash Book becomes one of the major sources of Ratibu's business insights.

Ratibu can eventually answer questions such as:

### Revenue

> How much did the business receive today?

### Shop performance

> Which shop generated the most revenue?

### Payment methods

> What percentage of payments were cash versus mobile money?

### Expenses

> What were the largest expenses this month?

### Trends

> Is revenue increasing or decreasing?

### Reconciliation

> Which shops have recurring cash discrepancies?

### Worker activity

> Which workers generated the most completed service value?

---

# 25. Financial vs Operational Metrics

Ratibu should distinguish between different measurements.

For example:

```text
Operational Activity
        │
        ├── Bookings
        ├── Completed Services
        └── Walk-ins

Financial Activity
        │
        ├── Payments
        ├── Expenses
        └── Refunds

Cash Position
        │
        ├── Opening Balance
        ├── Cash In
        ├── Cash Out
        └── Closing Balance
```

This prevents Ratibu from treating every business metric as the same thing.

---

# 26. Real-Time Financial Visibility

Because transactions are recorded throughout the day, Ratibu should be able to provide a current financial picture before EOD.

For example:

```text
TODAY

Revenue received      KSh 18,500
Cash received         KSh 10,000
Mobile money          KSh 7,000
Card                  KSh 1,500

Cash expenses         KSh 2,200

Expected cash         KSh 12,800
```

The owner does not have to wait until closing time to understand how the shop is performing.

---

# 27. Offline Transactions and Bookings

Ratibu will combine financial activity from both sources:

```text
APP BOOKINGS
     │
     └── Payments
             │
             ↓
          CASH BOOK
             ↑
     │
OFFLINE SALES
     │
     └── Payments
```

This means the business's financial picture includes both:

* Customers who used Ratibu
* Customers who did not

This is essential because Ratibu should represent the **business**, not merely activity that happened inside the application.

---

# 28. No Double Counting

The same financial event must not be counted twice.

For example:

```text
Booking
   ↓
Payment KSh 500
   ↓
Cash Book +500
```

EOD must not add another +500.

Similarly, an offline sale recorded during the day must not be re-entered as a large EOD sale.

EOD validates the day's transactions rather than replacing them.

---

# 29. Historical Financial Integrity

Financial records should be immutable or corrected through explicit compensating transactions rather than silently edited.

For example:

Incorrect:

```text
Payment
KSh 500
    ↓
Edit to
KSh 700
```

Preferred:

```text
Original Payment
+500

Correction / Additional Payment
+200
```

This preserves the financial history.

The precise accounting rules for corrections will be refined during implementation.

---

# 30. Core Domain Rules

The following rules are established:

### Rule 1

> Financial activity is represented as individual financial events.

### Rule 2

> Payments are separate from bookings.

### Rule 3

> Offline payments may be recorded throughout the business day.

### Rule 4

> Offline transactions are not restricted to EOD.

### Rule 5

> Every financial event belongs to a Shop.

### Rule 6

> The Cash Book records the chronological financial activity of a Shop.

### Rule 7

> Cash and non-cash payment methods must be distinguishable.

### Rule 8

> Cash expenses and other cash-outs reduce expected physical cash.

### Rule 9

> EOD is a reconciliation process, not a bulk transaction-entry mechanism.

### Rule 10

> EOD must not duplicate transactions already recorded during the day.

### Rule 11

> Expected closing cash is calculated from the Shop's financial events.

### Rule 12

> Actual closing cash is reported during EOD reconciliation.

### Rule 13

> Differences between expected and actual balances must remain visible.

### Rule 14

> Financial discrepancies should be explainable and explicitly resolved.

### Rule 15

> Each Shop has an independent EOD process.

### Rule 16

> Business-level financial information is derived from Shop-level financial events.

### Rule 17

> Financial corrections should preserve historical information rather than silently rewriting transactions.

### Rule 18

> Financial activity from both Ratibu bookings and offline sales contributes to business insights.

---

# 31. Resulting Financial Model

```text
                         SHOP
                           │
                    ┌──────┴──────┐
                    │             │
                BOOKINGS      OFFLINE SALES
                    │             │
                 PAYMENTS       PAYMENTS
                    │             │
                    └──────┬──────┘
                           ↓
                      CASH BOOK
                           │
             ┌─────────────┼─────────────┐
             │             │             │
          CASH IN       CASH OUT     OTHER EVENTS
             │             │             │
             └─────────────┼─────────────┘
                           ↓
                  EXPECTED POSITION
                           │
                           ↓
                     EOD RECONCILIATION
                           │
                  ┌────────┴────────┐
                  │                 │
               MATCH           DISCREPANCY
                  │                 │
                  ↓                 ↓
               CLOSED         EXPLANATION/
                               RESOLUTION
                           │
                           ↓
                    BUSINESS INSIGHTS
```

---

# 32. Consequences

## Positive consequences

* Financial activity is recorded at transaction level.
* Offline payments can be captured immediately.
* Businesses do not have to wait for EOD to record income.
* EOD provides verification rather than duplicate data entry.
* Cash discrepancies can be detected.
* Cash and non-cash balances can be distinguished.
* Multiple shops can reconcile independently.
* Business owners receive near-real-time financial visibility.
* Historical financial records remain traceable.
* Both online and offline business activity contributes to insights.

## Negative consequences

* Transaction-level financial tracking is more complex than a simple daily total.
* Users must record offline transactions throughout the day if they want complete real-time records.
* Reconciliation requires additional workflows.
* Discrepancy handling requires careful financial rules.
* Multiple payment methods introduce additional balances to track.
* Financial corrections require explicit transactions rather than casual editing.

These costs are accepted because financial accuracy and useful business insights are central to Ratibu's purpose.

---

# 33. Next Decision

The next ADR will define the **Business Insights and Analytics Model**.

It will determine:

* Which metrics Ratibu calculates
* Which insights are shown to owners
* Which insights are available to managers
* Shop-level vs business-level analytics
* Revenue metrics
* Booking metrics
* Client metrics
* Worker metrics
* Service performance
* Cash-flow information
* Expense analysis
* Client rate / spending metrics
* Trends and comparisons
* How offline and online activity are combined
* How Ratibu turns raw operational and financial data into actionable insights

The goal will be to ensure Ratibu does not merely **store business data**, but actually helps the owner **understand and make decisions about the business**.
