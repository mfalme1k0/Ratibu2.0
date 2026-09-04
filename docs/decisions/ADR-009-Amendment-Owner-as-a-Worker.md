# ADR-008 Amendment: Owner as a Worker / Solo-Shop Business

**Status:** Proposed
**Date:** 2026-09-01
**Related ADR:** ADR-008 — User Roles, Permissions and Business Access Model

---

# 1. Context

Ratibu must support businesses of different sizes.

Not every business will have employees.

A common business structure may be:

```text
Owner
  ↓
One Shop
  ↓
Owner performs the services
```

For example, a solo barber, hairdresser, consultant, mechanic, photographer, or other service provider may both own and operate the business.

In such a business, requiring the owner to create separate Owner and Worker accounts would introduce unnecessary complexity.

The owner should be able to use Ratibu for both management and daily operations using a single account.

---

# 2. Problem

The initial role model could be interpreted as:

```text
Owner
Worker
Manager
```

as mutually exclusive roles.

That would create an unnecessary problem for solo businesses.

For example:

```text
Fidel
  ↓
Owner account

But Fidel also performs:
- Bookings
- Services
- Payments
- Offline sales
- Client management
- EOD
```

Creating:

```text
Owner account
+
Worker account
```

for the same person would be cumbersome and could create duplicate identities and historical records.

---

# 3. Decision

Ratibu will **not treat Owner, Manager and Worker as mutually exclusive identities**.

Instead:

> A User represents a person, while roles and capabilities determine what that person can do.

An Owner may therefore also act as a Worker.

Conceptually:

```text id="7f4x9h"
USER
 │
 ├── OWNER ROLE
 │
 └── WORKER PROFILE
```

The Owner does not need a second account to perform services.

---

# 4. Solo Business Model

A solo business can therefore be represented as:

```text id="r7o3qk"
Business
   │
   └── Shop
         │
         └── Owner / Worker
```

The same user may:

* Create bookings
* Manage bookings
* Perform services
* Mark services as completed
* Record offline sales
* Record payments
* Record expenses
* Manage the Cash Book
* Perform EOD reconciliation
* View business insights
* Manage the Business

subject to the permissions associated with the Owner role.

---

# 5. Owner Acting as Worker

When the Owner performs a service, the service should be attributable to the Owner.

For example:

```text id="a7ct6w"
Booking
Client: Jane
Service: Haircut
Shop: Solo Shop

Worker:
Owner
```

This allows Ratibu to calculate:

* Services performed by the owner
* Revenue attributed to those services
* Client retention
* Average transaction value
* Service popularity
* Other relevant operational metrics

The system should not require a separate Worker account.

---

# 6. Optional Worker Profile

A Worker profile should be considered a capability/profile attached to a User rather than a completely separate identity.

Conceptually:

```text id="0v6sl6"
User
 │
 ├── Authentication Identity
 │
 ├── Roles
 │
 └── Worker Profile (optional)
```

This allows:

```text id="3ec7n4"
Owner
 └── Worker Profile
```

and:

```text id="6c75qv"
Worker
 └── Worker Profile
```

to share the same underlying identity model.

---

# 7. Worker Profile

The Worker Profile may contain information relevant to performing services, such as:

* Worker status
* Assigned Shops
* Services the worker can perform
* Availability
* Operational settings

The exact Worker Profile model will be defined during the domain-model ADR.

---

# 8. Owner and Worker Permissions

When the same user has multiple roles, their effective permissions should be the union of the permissions granted by those roles, subject to business security rules.

For example:

```text id="nq5l9k"
Owner
+
Worker
```

means the user can access both:

```text id="h84x6c"
Business Management
```

and:

```text id="v0e9gq"
Worker Operations
```

The system should not create two accounts or force the user to log out and switch accounts.

---

# 9. Owner's Interface

A solo owner should receive a unified experience.

For example:

```text id="x0rv9c"
HOME

Today's Business
────────────────────

Revenue        KSh 8,500
Bookings       12
Completed       9
Outstanding    KSh 1,500

Today's Work
────────────────────

10:00  Jane     Haircut
11:00  John     Beard Trim
13:00  Mary     Hair Coloring

Business Insights
────────────────────

Revenue ↑ 12%
Returning Clients ↑ 8%

Cash Book
EOD
Clients
Settings
```

The owner should not have to choose:

```text id="5f6s5t"
"Owner Mode"
```

versus:

```text id="4h9v6e"
"Worker Mode"
```

for ordinary operations.

---

# 10. Separation of Responsibilities

Although one person may hold multiple roles, Ratibu should still maintain conceptual separation between responsibilities.

For example:

```text id="2zy4gp"
Owner capability
→ Manage business

Worker capability
→ Perform services
```

This allows the same user to perform both activities without collapsing the domain concepts.

---

# 11. Solo EOD

A solo owner may perform their own EOD reconciliation.

For example:

```text id="0w6b9r"
Owner
  ↓
Records transactions during the day
  ↓
Performs EOD
  ↓
Counts actual cash
  ↓
Confirms closing balance
```

No Manager account is required.

The Owner's authorization is sufficient.

---

# 12. Solo Financial Workflow

A typical solo business day may look like:

```text id="yp9vl4"
08:00
Opening Cash
KSh 2,000

10:00
Booking completed
KSh 1,000 cash

11:30
Walk-in
KSh 500 cash

13:00
Business expense
KSh 300 cash

15:00
Booking payment
KSh 1,500 mobile money

18:00
EOD

Expected Cash:
KSh 3,200

Actual Cash:
KSh 3,200

Status:
RECONCILED
```

The owner performed every role in this workflow.

---

# 13. Growth Without Migration

The model must allow the business to grow naturally.

### Stage 1 — Solo

```text id="x1k3cd"
Owner
  │
  └── Shop
       └── Owner as Worker
```

### Stage 2 — Small Team

```text id="k9h4sp"
Owner
  │
  └── Shop
       ├── Owner as Worker
       ├── Worker
       └── Worker
```

### Stage 3 — Managed Shop

```text id="fd6k6j"
Owner
  │
  └── Shop
       ├── Manager
       ├── Owner as Worker
       └── Workers
```

### Stage 4 — Multiple Shops

```text id="5t9r8k"
Owner
  │
  ├── Shop A
  │    ├── Manager
  │    └── Workers
  │
  └── Shop B
       ├── Manager
       └── Workers
```

The underlying Business remains the same.

---

# 14. Multiple Shops and Owner as Worker

An Owner may also work at one or more Shops.

For example:

```text id="qg0hbc"
Owner
 │
 ├── Owns Business
 │
 ├── Shop A
 │    └── Owner works here
 │
 ├── Shop B
 │    └── Manager + Workers
 │
 └── Shop C
      └── Workers
```

This should be supported without creating duplicate user identities.

---

# 15. Insights for Solo Owners

The analytics system should adapt naturally.

For a solo owner, worker-level insights can become personal operational insights.

For example:

```text id="2x0drm"
Your services this month:
84

Your average transaction:
KSh 850

Your returning-client rate:
72%
```

The system should not display confusing comparisons such as:

```text id="v2fh48"
Worker ranking:
1. You
```

when there are no other workers.

---

# 16. Avoiding Unnecessary Complexity

The solo-owner experience should remain simple.

The system should not force a solo owner to manage:

* Employee accounts
* Manager permissions
* Worker schedules
* Team management

until those features are actually relevant.

For example:

```text id="tq4m8p"
Solo business

Home
Bookings
Clients
Cash Book
Insights
Settings
```

As workers are added:

```text id="8u6c9a"
Team
Workers
Schedules
Permissions
```

can naturally become visible.

---

# 17. Role Model

The resulting role model is:

```text id="v6tr1c"
                    USER
                      │
             ┌────────┼────────┐
             │        │        │
           OWNER   MANAGER   WORKER
             │        │        │
             └────────┼────────┘
                      │
               PERMISSIONS
                      │
                  SHOP SCOPE
```

Roles are capabilities assigned to a User rather than mutually exclusive account types.

---

# 18. Core Domain Rule

The following rule is added to ADR-008:

> A User may hold multiple roles within a Business.

Specifically:

> An Owner may also have a Worker Profile and perform services without creating a separate Worker account.

---

# 19. Additional Core Rules

### Rule 18

> Owner, Manager and Worker are roles/capabilities, not mutually exclusive identities.

### Rule 19

> A User may act as both Owner and Worker.

### Rule 20

> A User does not need multiple accounts to perform multiple roles.

### Rule 21

> An Owner acting as a Worker may be attributed as the Worker on completed services.

### Rule 22

> A solo business does not require a Manager or separate Worker account.

### Rule 23

> The system must support a business growing from one Owner-operated Shop to multiple Shops and workers without changing the Business identity.

### Rule 24

> The user interface should adapt to the user's actual responsibilities rather than forcing explicit mode switching.

---

# 20. Consequences

## Positive consequences

* Solo businesses are first-class Ratibu customers.
* Owners can perform their own services naturally.
* No duplicate accounts are required.
* Historical attribution remains accurate.
* Businesses can grow without migrating between product models.
* The same authorization system works for solo and multi-Shop businesses.
* Worker functionality can be introduced gradually as a business grows.
* The user experience remains simpler for small businesses.

## Negative consequences

* Role handling becomes more sophisticated.
* Authorization must correctly combine multiple roles.
* The data model must distinguish User identity from Worker Profile.
* UI decisions must account for users with multiple responsibilities.
* Testing permission combinations becomes more important.

These costs are accepted because supporting solo operators is fundamental to Ratibu's target market.

---

# 21. Updated Ratibu Organizational Model

The resulting model is:

```text id="z4v8gq"
                         BUSINESS
                            │
              ┌─────────────┴─────────────┐
              │                           │
             USER                        SHOPS
              │                           │
       ┌──────┼──────┐             ┌──────┼──────┐
       │      │      │             │      │      │
     OWNER  MANAGER WORKER       SHOP A SHOP B SHOP C
       │      │      │
       └──────┴──────┘
              │
        SHOP ASSIGNMENT
              │
        WORKER PROFILE
```

For a solo business:

```text id="m8f5gc"
             BUSINESS
                 │
                SHOP
                 │
          ┌──────┴──────┐
          │             │
        OWNER        WORKER
          │             │
          └──────┬──────┘
                 │
              SAME USER
```

This is the preferred Ratibu model.
