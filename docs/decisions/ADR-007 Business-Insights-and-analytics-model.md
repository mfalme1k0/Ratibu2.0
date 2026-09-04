# ADR-007: Business Insights and Analytics Model

**Status:** Proposed
**Date:** 2026-09-01
**Decision Type:** Product / Architecture
**Project:** Ratibu

---

# 1. Context

Ratibu is intended to help small and growing businesses understand their operations and make better decisions.

The system now captures information from:

* Bookings
* Completed services
* Clients
* Workers
* Services
* Payments
* Offline sales
* Expenses
* Cash-book transactions
* End-of-day reconciliation

This creates a significant amount of useful business information.

However, simply displaying this information does not necessarily help a business owner make decisions.

For example:

```text
Revenue: KSh 185,000
Bookings: 247
Expenses: KSh 62,000
```

provides information, but the owner may actually want to know:

> "Is my business doing better than last month?"

or:

> "Which shop is performing best?"

or:

> "Which services make me the most money?"

or:

> "Why is revenue down?"

Ratibu should therefore transform operational and financial data into understandable business insights.

---

# 2. Problem

Traditional small-business software often presents raw tables, totals and reports without helping the owner interpret them.

Ratibu's objective is different.

The system should help answer questions such as:

* How is my business performing?
* Is revenue increasing?
* Which shop is performing best?
* Which services are most popular?
* Which services generate the most revenue?
* Which workers are busiest?
* Which clients are returning?
* How much money is actually being received?
* How much is outstanding?
* Where are expenses increasing?
* Are there cash discrepancies?
* What changed compared with the previous period?

The system must therefore distinguish between:

```text
DATA
  ↓
METRICS
  ↓
INSIGHTS
  ↓
ACTIONABLE INFORMATION
```

---

# 3. Decision

Ratibu will provide **business insights at multiple levels**.

The primary levels are:

1. Business
2. Shop
3. Worker
4. Service
5. Client
6. Financial

The owner will receive a high-level business overview and may drill down into the underlying dimensions.

---

# 4. Insight Philosophy

Ratibu will prioritize **actionable insights over information overload**.

The application should answer:

> "What should I pay attention to?"

rather than simply:

> "Here is every number in your database."

For example:

Instead of displaying only:

```text
Revenue
KSh 185,000
```

Ratibu may display:

```text
Revenue
KSh 185,000

↑ 12% compared with last month
```

The owner can then drill down to understand the reason.

---

# 5. Business Overview

The Business Dashboard will provide the owner with a consolidated overview across all Shops.

Initial metrics include:

* Revenue received
* Total expenses
* Net cash movement
* Number of bookings
* Completed services
* Cancellation rate
* No-show rate
* Outstanding payments
* Active clients
* Returning clients
* Top services
* Shop performance

The exact displayed metrics may vary depending on the business type.

---

# 6. Time Periods

Insights will support common time periods such as:

* Today
* Yesterday
* This week
* Last week
* This month
* Last month
* Custom period

Comparisons should be available where meaningful.

For example:

```text
September 2026
Revenue: KSh 250,000

August 2026
Revenue: KSh 220,000

Change:
+13.6%
```

---

# 7. Revenue

Ratibu will distinguish between different financial concepts.

At minimum:

```text
Service Value
Payments Received
Outstanding Amount
Expenses
```

For example:

```text
Completed Services:
KSh 250,000

Payments Received:
KSh 220,000

Outstanding:
KSh 30,000
```

This prevents Ratibu from presenting unpaid service value as cash received.

---

# 8. Revenue Received

The primary financial performance metric will be based on actual recorded payments.

For example:

```text
Payments Received
        ↓
Financial Revenue View
```

Both booking payments and offline payments contribute.

```text
Booking Payments
+
Offline Payments
=
Total Payments Received
```

---

# 9. Offline Activity

Offline activity will be included in business insights.

For example:

```text
Total Revenue
KSh 200,000

From Ratibu bookings:
KSh 125,000

From offline sales:
KSh 75,000
```

This allows the owner to understand how much of the business is being captured through the application and how much occurs outside the booking workflow.

Offline activity must not be treated as inferior data.

It is part of the actual business.

---

# 10. Online vs Offline Insight

Ratibu may provide insights such as:

```text
Revenue Sources

Bookings       63%
Offline        37%
```

This can help the owner understand customer behavior and adoption of the booking system.

However, the system should avoid implying that offline transactions are inherently worse.

The purpose is visibility, not judgment.

---

# 11. Shop Performance

For multi-shop businesses, the owner should be able to compare Shops.

For example:

```text
Shop Performance

Westlands
Revenue: KSh 120,000
Bookings: 140

Kilimani
Revenue: KSh 85,000
Bookings: 102

CBD
Revenue: KSh 65,000
Bookings: 78
```

The owner can then identify:

* Highest-performing shop
* Fastest-growing shop
* Shop with highest expenses
* Shop with highest booking volume
* Shop with highest average transaction value

---

# 12. Shop Comparison

Ratibu should support comparisons between Shops.

For example:

```text
Westlands
Revenue: +18%

Kilimani
Revenue: +4%

CBD
Revenue: -9%
```

The system can highlight:

> CBD revenue decreased 9% compared with the previous period.

This allows the owner to investigate the underlying causes.

---

# 13. Worker Insights

Worker insights should focus on operational performance rather than reducing workers to simple revenue rankings.

Potential metrics include:

* Services completed
* Bookings handled
* Average service value
* Client return rate
* Working hours where available
* Cancellation/no-show patterns
* Services performed
* Revenue attributed to completed services

For example:

```text
Mary
Services: 84
Service Value: KSh 62,000
Returning Clients: 71%

John
Services: 63
Service Value: KSh 51,000
Returning Clients: 78%
```

These metrics can help owners understand workload and performance.

---

# 14. Worker Performance Context

Worker metrics should be interpreted in context.

For example, comparing:

```text
Worker A → 80 services
Worker B → 40 services
```

does not necessarily mean Worker A is twice as productive.

Differences may result from:

* Working hours
* Service duration
* Service type
* Shop assignment
* Days worked
* Availability

Ratibu should therefore avoid presenting simplistic rankings without sufficient context.

---

# 15. Service Insights

Ratibu should track service performance.

For example:

```text
Haircut
Bookings: 120
Revenue: KSh 60,000

Hair Coloring
Bookings: 40
Revenue: KSh 100,000
```

This reveals an important distinction:

```text
Most popular
≠
Highest revenue
```

Both metrics are useful.

---

# 16. Service Popularity

Popularity may be measured using:

* Number of bookings
* Number of completed services
* Number of offline sales

For example:

```text
Most Performed Services

1. Haircut
2. Beard Trim
3. Braiding
```

This helps the owner understand customer demand.

---

# 17. Service Revenue

Revenue performance should be calculated separately.

For example:

```text
Highest Revenue Services

1. Hair Coloring
2. Braiding
3. Haircut
```

This prevents the business owner from assuming that the most frequently purchased service is necessarily the most financially valuable.

---

# 18. Average Transaction Value

Ratibu should calculate average transaction value where enough data exists.

For example:

```text
Revenue received:
KSh 120,000

Transactions:
200

Average:
KSh 600
```

The metric can be tracked over time.

For example:

```text
Last month:
KSh 540

This month:
KSh 600

Change:
+11.1%
```

This can help identify changes in customer spending behavior.

---

# 19. Client Insights

Client insights should help the owner understand customer behavior.

Potential metrics include:

* New clients
* Returning clients
* Active clients
* Inactive clients
* Visit frequency
* Average client spending
* Most valuable clients
* Client retention
* Last visit
* Services purchased

---

# 20. Client Rate

Ratibu will introduce a concept of **Client Rate**.

Client Rate represents a calculated measure of the client's economic value or spending behavior over a defined period.

It should not be interpreted as a fixed price assigned to the client.

For example:

```text
Jane

Visits: 8
Total spending: KSh 16,000

Average spend per visit:
KSh 2,000
```

This can help the owner understand customer value.

The exact Client Rate formula will be finalized after sufficient historical data and product testing.

---

# 21. Returning Clients

Ratibu should identify returning clients.

For example:

```text
This Month

New clients:
32

Returning clients:
84
```

The owner may then see:

```text
Returning client rate:
72%
```

This can become a useful indicator of customer retention.

---

# 22. Client Retention

Ratibu should track whether clients continue returning.

For example:

```text
January clients:
100

Clients who returned within 60 days:
68

Retention:
68%
```

Retention calculations should use clearly defined time windows.

---

# 23. Expenses

Ratibu will provide expense insights.

Potential metrics include:

* Total expenses
* Expense categories
* Expense trends
* Cash expenses
* Non-cash expenses
* Largest expense categories
* Shop-level expenses

For example:

```text
Expenses

Supplies        KSh 20,000
Transport       KSh 8,000
Utilities       KSh 12,000
Other           KSh 5,000
```

---

# 24. Expense Trends

Ratibu should identify significant changes.

For example:

```text
Supplies

August:
KSh 15,000

September:
KSh 22,000

Increase:
46.7%
```

The system may surface:

> Supplies spending increased significantly this month.

The owner can then investigate the underlying transactions.

---

# 25. Cash Position

Ratibu should provide a current expected cash position based on recorded transactions.

For example:

```text
Opening Cash       KSh 10,000
Cash In            KSh 25,000
Cash Out           KSh 8,000
────────────────────────────
Expected Cash      KSh 27,000
```

This is distinct from total revenue.

---

# 26. Cash Discrepancies

EOD reconciliation provides another important insight.

For example:

```text
Westlands

Expected:
KSh 27,000

Actual:
KSh 26,500

Difference:
-KSh 500
```

Ratibu may identify shops with recurring discrepancies.

For example:

> Westlands has recorded cash discrepancies on 6 of the last 10 business days.

This can be useful for operational investigation.

The system should present discrepancies neutrally and avoid automatically accusing workers of wrongdoing.

---

# 27. Booking Insights

Ratibu will track booking behavior.

Potential metrics include:

* Total bookings
* Completed bookings
* Cancelled bookings
* No-shows
* Completion rate
* Cancellation rate
* Average booking value
* Booking lead time
* Peak booking times

---

# 28. Peak Business Hours

Ratibu can analyze completed services and bookings by time.

For example:

```text
Most active period

14:00–17:00
```

This can help owners make decisions about:

* Worker scheduling
* Staffing
* Opening hours
* Promotions
* Appointment availability

---

# 29. Demand Patterns

Ratibu may identify recurring patterns.

For example:

```text
Saturday bookings
↑ 34% compared with weekday average
```

or:

> Saturday afternoons are consistently the busiest period.

These insights can help businesses optimize staffing.

---

# 30. Business Growth

Ratibu should track changes over time.

Potential growth indicators include:

* Revenue growth
* Client growth
* Booking growth
* Service growth
* Shop growth
* Average transaction value
* Returning-client rate

For example:

```text
Business Growth

Revenue       +14%
Clients       +9%
Bookings      +17%
Avg. Spend    +4%
```

---

# 31. Insight Confidence

Ratibu should avoid making strong conclusions from insufficient data.

For example, if only three transactions exist:

```text
Revenue increased 400%
```

may technically be true but not particularly useful.

Insights should therefore consider:

* Amount of data available
* Time period
* Comparison period
* Statistical significance where appropriate

When insufficient data exists, Ratibu should say so.

For example:

> Not enough data to identify a reliable trend yet.

---

# 32. Insights Should Be Explainable

Every important insight should be traceable to underlying information.

For example:

```text
Revenue increased 18%
        ↓
View details
        ↓
Hair Coloring +32%
Haircut +8%
Offline Sales +14%
```

The owner should be able to drill down from an insight into the underlying metrics and transactions.

This prevents Ratibu from becoming a "black box."

---

# 33. Insights vs Reports

Ratibu will distinguish between:

### Reports

Structured information.

Example:

```text
September Revenue Report
```

and:

### Insights

Interpretations of the information.

Example:

> Revenue increased 18% this month, primarily driven by Hair Coloring.

Reports answer:

> "What happened?"

Insights attempt to answer:

> "What does this mean?"

---

# 34. Owner Dashboard

The owner dashboard should prioritize the most important information.

A conceptual dashboard might contain:

```text
┌───────────────────────────────────────┐
│           BUSINESS OVERVIEW           │
├───────────────────────────────────────┤
│ Revenue       KSh 250,000    ↑ 14%    │
│ Expenses      KSh  72,000    ↑  5%    │
│ Net Movement  KSh 178,000    ↑ 18%    │
├───────────────────────────────────────┤
│ Bookings      284            ↑ 12%    │
│ Clients       164            ↑  8%    │
│ Avg. Spend    KSh 880        ↑  4%    │
├───────────────────────────────────────┤
│ KEY INSIGHTS                          │
│                                       │
│ • Kilimani is your fastest-growing    │
│   shop this month.                    │
│                                       │
│ • Hair Coloring generated the most    │
│   revenue.                            │
│                                       │
│ • Saturday is your busiest day.       │
│                                       │
│ • Westlands has 3 unresolved cash     │
│   discrepancies.                      │
└───────────────────────────────────────┘
```

---

# 35. Shop Dashboard

Each Shop should have its own operational and financial view.

For example:

```text
WESTLANDS

Today's Revenue
KSh 18,500

Bookings
24

Completed
20

Outstanding
KSh 3,000

Expected Cash
KSh 12,800

EOD
Not Yet Closed
```

The shop view should not require the owner to mentally filter a business-wide dashboard.

---

# 36. Multi-Shop Insights

For businesses with multiple Shops, Ratibu should provide:

### Consolidated view

```text
Business
   ↓
All Shops
```

and:

### Comparative view

```text
Business
   ├── Westlands
   ├── Kilimani
   └── CBD
```

The owner can switch between:

```text
TOTAL BUSINESS
```

and:

```text
INDIVIDUAL SHOP
```

without creating separate businesses.

---

# 37. Insights and Offline Data

Offline activity must contribute to analytics.

For example:

```text
Total Services
    │
    ├── Booked through Ratibu
    └── Recorded offline
```

and:

```text
Total Payments
    │
    ├── Booking payments
    └── Offline payments
```

This gives the owner a complete picture of business activity.

---

# 38. Data Freshness

Ratibu should distinguish between:

* Real-time/current metrics
* Reconciled historical metrics
* Unreconciled data

For example:

```text
Today's Revenue
Based on transactions recorded so far
```

while:

```text
Yesterday's Revenue
EOD reconciled
```

This distinction prevents users from assuming that today's figures are final.

---

# 39. No Artificial Precision

Ratibu should avoid presenting calculated metrics with unnecessary precision.

For example:

```text
Client retention:
68%
```

is preferable to:

```text
Client retention:
67.843217%
```

The goal is understandable business information, not mathematical decoration.

---

# 40. Insights Must Respect Business Context

Different businesses may have different operational models.

For example:

```text
Hair Salon
→ Worker performance is highly relevant.

Retail Shop
→ Worker-service relationships may be less relevant.

Consultancy
→ Booking duration may be more important.
```

Therefore, Ratibu should establish a flexible insight system rather than assuming every business uses the same metrics.

The initial implementation will focus on metrics common to the first supported business models.

---

# 41. Insights and Data Integrity

Insights must be based on valid underlying records.

For example:

```text
Cancelled Booking
```

must not be counted as:

```text
Completed Service
```

and:

```text
Booking
```

must not automatically count as:

```text
Payment Received
```

Similarly:

```text
Expected Cash
```

must not be presented as:

```text
Actual Cash
```

when EOD has not been completed.

---

# 42. Business Intelligence Architecture

Conceptually:

```text
                  RAW BUSINESS ACTIVITY
                           │
          ┌────────────────┼────────────────┐
          │                │                │
      OPERATIONS        FINANCE          CLIENT DATA
          │                │                │
      Bookings         Payments          Visits
      Services         Expenses          Spending
      Workers          Cash Book         Retention
          │                │                │
          └────────────────┼────────────────┘
                           ↓
                       METRICS
                           ↓
                    COMPARISONS
                           ↓
                       INSIGHTS
                           ↓
                    OWNER ACTION
```

---

# 43. Core Insight Categories

The initial Ratibu insight categories are:

```text
1. Business Performance
2. Revenue
3. Expenses
4. Cash Position
5. Bookings
6. Clients
7. Workers
8. Services
9. Shops
10. Growth
11. Reconciliation
```

These categories may expand as Ratibu learns more about its users.

---

# 44. Core Domain Rules

### Rule 1

> Insights are derived from operational and financial data.

### Rule 2

> Financial insights are based on actual financial events.

### Rule 3

> Bookings are not treated as payments.

### Rule 4

> Offline activity contributes to business insights.

### Rule 5

> Business-level insights aggregate Shop-level activity.

### Rule 6

> Shop-level insights remain independently available.

### Rule 7

> Historical data must remain stable when current configurations change.

### Rule 8

> Important insights must be explainable through underlying metrics.

### Rule 9

> Insights should avoid conclusions based on insufficient data.

### Rule 10

> Current/unreconciled information must be distinguishable from finalized historical information.

### Rule 11

> Worker performance should be presented with appropriate context.

### Rule 12

> The dashboard should prioritize actionable information over raw data volume.

---

# 45. Consequences

## Positive consequences

* Ratibu becomes more than a booking and bookkeeping application.
* Owners receive a consolidated view of their businesses.
* Multi-shop owners can compare branches.
* Offline activity contributes to the complete business picture.
* Owners can identify trends and changes.
* Client behavior becomes measurable.
* Service performance becomes measurable.
* Financial performance can be understood alongside operational performance.
* Cash discrepancies become visible.
* Insights can guide staffing and business decisions.
* The system creates a foundation for future predictive features.

## Negative consequences

* Analytics adds considerable domain and technical complexity.
* Metrics require carefully defined formulas.
* Poor-quality underlying data can produce misleading insights.
* Different business types may require different metrics.
* Historical data must be preserved consistently.
* Insight generation may require aggregation and optimization as businesses grow.
* The dashboard must balance usefulness against information overload.

These costs are accepted because business intelligence is a central part of Ratibu's product vision.

---

# 46. Next Decision

The next ADR will define the **User Roles, Permissions and Business Access Model**.

It will establish how Ratibu supports:

* Owners
* Managers
* Workers
* Multiple owners where applicable
* Shop-level permissions
* Business-wide permissions
* Worker access
* Financial permissions
* EOD permissions
* Insight visibility
* Client access
* Multi-shop access
* User invitations
* Worker accounts
* Ownership and administration

This will determine **who is allowed to perform each action** within Ratibu.
