# ADR-008: Permission Model, Access Scoping and Role-Based Experience

**Status:** Proposed
**Date:** 2026-09-01 (Revised)
**Decision Type:** Product / Architecture
**Project:** Ratibu
**Supersedes:** Nothing. **Builds on:** ADR-002 (Accounts, Business Memberships, Roles and Access Control)

---

# 0. Relationship to ADR-002

ADR-002 established the identity and membership foundation for Ratibu:

```text
User Account
      ↓
Business Membership
      ↓
Role + Shop Assignment
```

It defined:

* **User Account** — a person who can authenticate with Ratibu.
* **Business Membership** — the link between a User Account and a specific Business, carrying a Role and Shop Assignment(s).
* **Worker Profile** — a record of a worker that can exist independently of a User Account, so a business can track a worker's activity before (or without) that worker ever logging into Ratibu.
* The three initial roles: **Owner**, **Manager**, **Worker**.
* The decision not to introduce a dedicated **Cashier** role, treating financial responsibilities as permissions instead.

This ADR does not redefine any of that. It answers the questions ADR-002 deliberately left open:

* What can each role concretely do? (the permission catalog)
* How is a permission scoped — to the whole Business, to specific Shops, or to the user's own activity?
* What does the application experience look like for each role (navigation, dashboards, notifications)?
* How is financial and EOD authorization enforced, and how is it audited?

Wherever this ADR refers to a "user," it means a **User Account operating under a Business Membership**, as defined in ADR-002. A Worker Profile with no linked User Account has no permissions to speak of — someone with an account (an Owner, Manager, or the worker themselves once invited) records activity on their behalf, exactly as ADR-002 §7–9 describes.

---

# 1. Context

Ratibu must support businesses of different sizes.

A business may consist of:

```text
One Owner
    ↓
One Shop
    ↓
A Few Workers
```

or:

```text
One Owner
    ↓
Multiple Shops
    ↓
Many Workers
```

The system should also support a business growing over time.

For example:

```text
1 Shop
   ↓
3 Workers
   ↓
2 Shops
   ↓
15 Workers
   ↓
5 Shops
   ↓
40 Workers
```

The owner should not need to migrate to a different system as the business grows.

At the same time, workers should not automatically receive access to sensitive business information simply because they work for the business.

ADR-002 gives Ratibu the structure needed to represent this (Business Membership, Role, Shop Assignment). This ADR defines what actually happens with that structure at runtime.

---

# 2. Problem

Without a concrete permission model, several problems can occur.

For example:

```text
Worker
   ↓
Can see all business revenue
```

may expose information the owner does not want every worker to see.

Similarly:

```text
Worker
   ↓
Can edit financial transactions
```

could compromise financial records.

On the other hand, making workers too restricted would make Ratibu difficult to use operationally.

For example, a worker should be able to:

* View their bookings
* Manage their assigned appointments
* Record an offline sale
* Record a payment where authorized
* View relevant client information

without necessarily being able to:

* Add another Shop
* Delete financial history
* View every Shop's revenue
* Change business ownership
* Manage other workers

Ratibu therefore requires a concrete permission catalog, scoped appropriately, layered on top of ADR-002's membership model.

---

# 3. Decision

Ratibu will define **concrete permissions and scoping rules** for the roles established in ADR-002.

A user's effective access is determined by:

```text
ROLE (from Business Membership, per ADR-002)
+
PERMISSION (defined in this ADR)
+
SCOPE (Business-wide or specific Shop Assignment(s), per ADR-002)
```

The application's navigation, dashboards, and notifications will also adapt to the user's role, as defined in this ADR.

---

# 4. Business as the Access Boundary

The Business is the highest-level organizational boundary, as established in ADR-002.

Conceptually:

```text
BUSINESS
   │
   ├── BUSINESS MEMBERSHIPS
   │
   ├── SHOPS
   │
   ├── CLIENTS
   │
   ├── SERVICES
   │
   ├── BOOKINGS
   │
   └── FINANCIAL DATA
```

A user's access to any of this is always mediated by a Business Membership. A user should not automatically have access to data belonging to another Business, even if they hold a Business Membership elsewhere (ADR-002 §13).

---

# 5. Shop as the Operational Boundary

Each Shop operates within a Business.

```text
Business
   │
   ├── Shop A
   │
   ├── Shop B
   │
   └── Shop C
```

Operational activity is associated with a Shop, per ADR-003's structural rules. This includes:

* Bookings
* Services performed
* Payments
* Cash-book transactions
* Expenses
* EOD reconciliation

This allows access to be scoped appropriately, using the Shop Assignment mechanism from ADR-002 §8.

---

# 6. Owner

The Owner is the highest-privileged business role.

The Owner may:

* View the entire Business
* View all Shops
* Add Shops
* Remove/deactivate Shops
* Invite users (per the Worker Profile → Invite → User Account flow in ADR-002 §7)
* Manage roles and Business Memberships
* Assign workers and managers to Shops
* Configure services
* View financial information
* View business insights
* View Shop insights
* Manage EOD permissions
* Review discrepancies
* Manage business settings

The Owner is responsible for the overall business.

---

# 7. Manager

A Manager is responsible for operational management of one or more Shops, as defined by their Shop Assignment (ADR-002 §8).

For example:

```text
Business
   │
   ├── Westlands
   │      └── Manager A
   │
   ├── Kilimani
   │      └── Manager B
   │
   └── CBD
          └── Manager A
```

This allows a manager to oversee multiple branches where appropriate.

---

# 8. Manager Permissions

Managers may be permitted to:

* View assigned Shops
* View bookings
* Manage bookings
* View assigned workers
* Assign workers to bookings
* View clients
* Record offline transactions
* Record payments
* Record expenses
* Perform EOD reconciliation
* View Shop-level insights
* Review Shop financial activity

Managers should not automatically have:

* Business ownership
* Full access to every Shop
* Ability to transfer ownership
* Ability to permanently delete financial history
* Ability to modify critical Business settings

---

# 9. Worker

Workers are primarily operational users.

Per ADR-002 §7, a Worker exists first as a **Worker Profile**, which may or may not have a linked **User Account**. The permissions below apply once a Worker Profile has an associated User Account and Business Membership — that is, once the worker can actually log into Ratibu. A Worker Profile without an account has no permissions of its own; an authorized user (Owner, Manager, or the worker after being invited) records activity on the profile's behalf.

Once logged in, a Worker may be assigned to one or more Shops (ADR-002 §8) and may:

* View assigned bookings
* View relevant clients
* Manage their assigned bookings
* Mark services as completed
* Record authorized offline transactions
* Record authorized payments
* View their own operational activity
* View limited Shop information where permitted

Workers should not automatically receive full financial visibility.

---

# 10. Worker Financial Access

Financial permissions should be configurable, per Business, rather than fixed.

For example:

```text
Worker A
Can record payment
Cannot view total revenue

Worker B
Can record payment
Can view daily Shop revenue

Manager
Can view Shop financial reports
Can reconcile EOD

Owner
Can view all financial information
```

Consistent with **ADR-002 §16**, Ratibu does not introduce a dedicated Cashier role for this. Financial responsibilities such as recording cash, viewing the cash book, or closing the day are granted as individual permissions to whichever role — typically Manager, sometimes a specific Worker — the business actually trusts with them.

---

# 11. Permission Model

Roles provide the default permission set. Permissions may include:

### Business

* View Business
* Edit Business
* Manage Shops
* Manage Business Memberships (invite, assign roles, assign shops — per ADR-002)

### Bookings

* Create Booking
* View Booking
* Edit Booking
* Cancel Booking
* Complete Booking

### Clients

* View Client
* Create Client
* Edit Client

### Services

* View Services
* Create Service
* Edit Service
* Disable Service

### Finance

* Record Payment
* Record Expense
* View Financial Data
* Correct Financial Records

### Cash Book

* View Cash Book
* Record Cash Transaction
* Reconcile EOD
* Resolve Discrepancy

### Insights

* View Personal Insights
* View Shop Insights
* View Business Insights

---

# 12. Permission Scope

A permission is not enough by itself. It also has a scope, determined by the user's Role and Shop Assignment (ADR-002 §8–9).

For example:

```text
VIEW_FINANCIAL_DATA
```

could mean:

```text
Owner → Entire Business

Manager → Assigned Shops

Worker → Own transactions
```

Therefore:

```text
Permission
+
Scope
=
Effective Access
```

---

# 13. Shop Assignment

Workers and Managers may be assigned to Shops, per ADR-002 §8.

For example:

```text
Mary
Role: Worker

Assigned Shops:
- Westlands
- Kilimani
```

Mary can then access the operational data relevant to those Shops. A Shop assignment may be removed without deleting the user's account, Business Membership, or historical activity.

---

# 14. Historical Access

Removing a user from a Shop should not erase historical records.

For example:

```text
Mary
worked at Westlands
January–June
```

After being removed from the Shop:

```text
Mary
No longer assigned to Westlands
```

Historical transactions and services performed by Mary must remain intact.

---

# 15. User Deactivation

Users should preferably be **deactivated rather than deleted** when they leave a business — deactivating the Business Membership, not the underlying Worker Profile or User Account (which may be shared across multiple businesses per ADR-002 §13).

For example:

```text
Worker
   ↓
Membership Deactivated
```

instead of:

```text
Worker
   ↓
Account Deleted
```

Historical records must continue to reference the person responsible for past activity.

---

# 16. Ownership

A Business must have an Owner. The Owner has authority over:

* Business configuration
* Shops
* Business Memberships
* Roles
* Financial information
* Business-level insights

Ownership should be distinct from employment. A Worker is not an Owner simply because they have been assigned to many Shops.

---

# 17. Multiple Owners

Per **ADR-002 §14**, the underlying Business Membership model permits more than one Business Membership to hold the Owner role. The initial implementation restricts a Business to a single **Primary Owner** for simplicity of onboarding and account management.

Where the Primary Owner may eventually need distinct authority not shared by future co-owners — for example, billing, account deletion, or transfer of primary ownership — that authority is not yet defined and is left for a future ADR once multi-owner support is actually built.

---

# 18. Business Growth

The permission model must work for businesses of different sizes.

### Small business

```text
Owner
  │
  └── Shop
       ├── Worker
       └── Worker
```

### Growing business

```text
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

### Larger business

```text
Owner
  │
  ├── Shop A
  │    ├── Manager
  │    └── Workers
  │
  ├── Shop B
  │    ├── Manager
  │    └── Workers
  │
  ├── Shop C
  │    └── Manager
  │
  └── Shop D
       ├── Manager
       └── Workers
```

The same application, and the same Business Membership model from ADR-002, supports all three.

---

# 19. One Application, Multiple Experiences

Ratibu will initially use **one application with role-based experiences** rather than creating separate applications for owners and workers.

```text
                    RATIBU
                      │
            ┌─────────┴─────────┐
            │                   │
         OWNER              WORKER
            │                   │
       Business View       Operational View
            │                   │
       All Shops          Assigned Shop(s)
            │                   │
       Insights           Today's Work
            │                   │
       Financials         Bookings
```

The underlying platform, and the underlying Business Membership model, remain the same. The interface adapts to the user's role and permissions.

---

# 20. Owner Experience

The Owner's primary experience should focus on:

```text
BUSINESS
   ↓
PERFORMANCE
   ↓
DECISIONS
```

The Owner should quickly access:

* Business overview
* Shop comparison
* Revenue
* Expenses
* Cash position
* Insights
* Clients
* Workers
* Services
* EOD status
* Alerts/discrepancies

---

# 21. Worker Experience

The Worker's primary experience should focus on:

```text
TODAY
   ↓
WORK
   ↓
CLIENTS
   ↓
TRANSACTIONS
```

The Worker should quickly access:

* Today's bookings
* Current appointments
* Assigned clients
* Service completion
* Offline sales
* Payment recording
* Relevant Shop information

The Worker should not need to navigate through business analytics just to perform a normal work task.

---

# 22. Manager Experience

The Manager sits between Owner and Worker.

The Manager's primary experience should focus on:

```text
SHOP
   ↓
OPERATIONS
   ↓
STAFF
   ↓
FINANCE
```

A Manager should be able to see what is happening within their assigned Shop(s) and perform authorized administrative operations.

---

# 23. Financial Security

Financial permissions require additional protection.

Operations such as:

* Correcting financial records
* Resolving discrepancies
* Reversing payments
* Performing EOD reconciliation

should require appropriate authorization.

A Worker who can record a KSh 500 payment should not automatically be able to alter the entire Shop's financial history.

---

# 24. Auditability

Important actions should be attributable to the user who performed them, via their Business Membership.

For example:

```text
Payment recorded
Amount: KSh 500
Shop: Westlands
Performed by: Mary
Time: 14:32
```

Similarly:

```text
EOD reconciled
Shop: Westlands
Performed by: Manager A
Time: 20:14
```

This provides accountability, improves reliability, and directly supports the discrepancy investigation described in ADR-006 (e.g., identifying which shop, and potentially which user, is associated with recurring cash discrepancies).

---

# 25. Role Changes

A user's role within a Business Membership may change over time.

For example:

```text
Worker
   ↓
Manager
```

or:

```text
Manager
   ↓
Worker
```

**When a Worker is promoted to Manager, their existing Shop Assignments carry over by default** — the shops they were already working at become the shops they now manage, rather than requiring the Owner to re-assign from scratch. The Owner may adjust the Shop Assignment afterward if the new Manager's scope should differ from their prior Worker assignment.

Historical actions must continue to identify the user and remain associated with the correct historical role context — i.e., an action taken while the user was a Worker is not retroactively reattributed as a Manager action.

---

# 26. Invitation Model

Owners and authorized Managers may invite users to the Business, following the flow established in **ADR-002 §7**:

```text
Worker Profile
      │
      ↓
Invite Worker
      │
      ↓
User Account
      │
      ↓
Business Membership (Role + Shop Assignment)
```

A worker may exist purely as a Worker Profile — tracked in bookings and financial records — long before, or without ever, being invited. Once invited and the invitation is accepted, a Business Membership is created linking the resulting User Account to the existing Worker Profile, so historical activity recorded against the profile remains attributed to the same person.

The user does not automatically gain access to every Shop in the Business upon accepting an invitation. Shop assignment is explicit.

---

# 27. User Access and Multiple Shops

A user may have different access across Shops.

For example:

```text
Mary

Westlands:
Worker

Kilimani:
Worker

CBD:
No Access
```

A Manager may have:

```text
John

Westlands:
Manager

Kilimani:
Manager

CBD:
No Access
```

This provides flexibility for growing businesses.

---

# 28. Business Insights Permissions

Insights should also respect scope.

For example:

```text
Owner
→ Business Insights
→ All Shop Insights

Manager
→ Assigned Shop Insights

Worker
→ Personal / limited operational insights
```

A Worker should not automatically see:

```text
Total Business Revenue
```

unless the Owner explicitly grants that permission.

---

# 29. Client Data Access

Client information should also be scoped appropriately.

A Worker may need:

```text
Client Name
Contact Information
Booking History
Relevant Service Information
```

but may not need:

```text
Complete Business-wide Client Database
```

unless their role requires it.

Managers may have broader access within assigned Shops. Owners may have Business-wide client visibility, consistent with ADR-004's decision that Clients belong to the Business.

---

# 30. Principle of Least Privilege

Ratibu will follow the principle:

> Users should receive the minimum access necessary to perform their responsibilities.

Therefore:

```text
Worker
≠
Mini Owner
```

and:

```text
Manager
≠
Owner
```

Roles should provide useful capabilities without unnecessarily exposing sensitive information.

---

# 31. No Separate Owner and Worker Codebases

Ratibu will not initially maintain:

```text
Ratibu Owner App
Ratibu Worker App
```

as separate codebases.

Instead:

```text
Ratibu
   │
   ├── Owner Experience
   ├── Manager Experience
   └── Worker Experience
```

This reduces development duplication, maintenance overhead, feature divergence, and deployment complexity. A separate application may be considered later if actual product requirements justify it.

---

# 32. Role-Based Navigation

The application should adapt navigation to the user's role.

### Owner

```text
Home
Insights
Shops
Bookings
Clients
Team
Cash Book
Reports
Settings
```

### Manager

```text
Home
Bookings
Clients
Team
Cash Book
Shop Insights
EOD
```

### Worker

```text
Today
Bookings
Clients
Transactions
Profile
```

These are conceptual examples rather than final UI decisions.

---

# 33. Role-Based Notifications

Notifications should also respect user responsibilities.

### Owner

```text
Revenue decreased this month
Shop has unresolved discrepancy
New worker joined
```

### Manager

```text
EOD has not been completed
Worker schedule conflict
Payment discrepancy
```

### Worker

```text
Upcoming booking
Client arrived
Payment needs recording
```

This keeps notifications relevant.

---

# 34. Multi-Shop Owner View

An Owner managing multiple Shops should not have to log into separate accounts.

The Owner should see:

```text
My Business
    │
    ├── All Shops
    │
    ├── Westlands
    ├── Kilimani
    ├── CBD
    └── Other Shops
```

The Owner can switch between Business View and Shop View without changing accounts.

---

# 35. Single-Shop Owner View

For a business with one Shop, the system should remain simple.

The Owner should not be forced to navigate unnecessary multi-Shop concepts.

```text
Owner
  ↓
Business
  ↓
Shop
```

The same underlying model, including the full Business Membership structure from ADR-002, supports this without requiring a separate product — the complexity simply doesn't surface in the UI (per ADR-002 §17, "Simple by Default").

---

# 36. Permissions Are Business-Specific

Ratibu should avoid assuming that every business organizes itself identically.

One business may have:

```text
Owner
Manager
Workers
```

while another may use:

```text
Owner
Workers
```

The system should allow roles and permissions to support both structures.

---

# 37. Core Access Model

```text
                         BUSINESS
                            │
                ┌───────────┴───────────┐
                │                       │
              OWNER                   SHOPS
                                        │
                         ┌──────────────┼──────────────┐
                         │              │              │
                       SHOP A         SHOP B         SHOP C
                         │              │              │
                    ┌────┴────┐    ┌────┴────┐    ┌────┴────┐
                    │         │    │         │    │         │
                 MANAGER   WORKERS MANAGER WORKERS MANAGER WORKERS
```

Access is determined by:

```text
User Account
 ↓
Business Membership (ADR-002)
 ↓
Role
 ↓
Shop Assignment
 ↓
Permission (this ADR)
 ↓
Scope (this ADR)
```

---

# 38. Core Domain Rules

### Rule 1
> Every user's access to a Business is mediated by a Business Membership (ADR-002).

### Rule 2
> A Business has Shops.

### Rule 3
> A Business Membership carries a Role.

### Rule 4
> Roles determine default permissions.

### Rule 5
> Permissions are scoped to the Business and/or the user's assigned Shops.

### Rule 6
> Owners have Business-wide administrative access.

### Rule 7
> Managers have access to assigned Shops according to their permissions.

### Rule 8
> Workers have operational access appropriate to their assigned Shops, once their Worker Profile has a linked User Account.

### Rule 9
> Users do not automatically gain access to every Shop in a Business.

### Rule 10
> Historical activity remains associated with the user (and their role at the time) who performed it.

### Rule 11
> Deactivating a Business Membership does not delete historical records, the underlying Worker Profile, or the User Account.

### Rule 12
> Financial operations require appropriate authorization.

### Rule 13
> EOD reconciliation requires appropriate authorization.

### Rule 14
> Business-level insights require Business-level permission.

### Rule 15
> Shop-level insights require access to the relevant Shop.

### Rule 16
> Ratibu initially uses one application with role-based experiences rather than separate Owner and Worker applications.

### Rule 17
> The access model must support businesses ranging from one Shop to multiple Shops and many workers.

### Rule 18
> A dedicated Cashier role is not introduced; financial responsibilities are granted as permissions on top of existing roles (per ADR-002 §16).

### Rule 19
> When a user's role changes, their existing Shop Assignments carry over by default and may be adjusted afterward.

---

# 39. Consequences

## Positive consequences

* One application can support businesses of different sizes.
* Owners can manage multiple Shops from one account.
* Workers receive only the access they need.
* Managers can operate at Shop level.
* Financial data receives additional protection.
* Historical activity remains attributable.
* The system can scale from a tiny business to a larger multi-Shop business.
* Separate Owner and Worker applications are unnecessary initially.
* Future custom permissions can be introduced without replacing the basic model.
* A single, consistent identity/membership model (ADR-002) underlies all permission and scoping logic, avoiding drift between "who a user is" and "what a user can do."

## Negative consequences

* Role-based access introduces additional complexity.
* Shop-level permissions require careful authorization logic.
* Financial permissions require additional security considerations.
* Role changes must preserve historical accountability.
* The interface becomes role-dependent.
* Testing authorization becomes an important part of development.

These costs are accepted because multi-user and multi-Shop support is fundamental to Ratibu's intended market.

---

# 40. Next Decision

The next ADR should define the **Offline-First and Synchronization Architecture**.

This is particularly important for Ratibu because the application is intended for small businesses where connectivity may be unreliable, and because this ADR's permission model, and ADR-002's membership model, both need to behave predictably when two devices modify the same Business Membership, Role, or Shop Assignment while offline.

The ADR should determine:

* What works completely offline
* What data is stored locally
* How bookings work without internet
* How offline payments are recorded
* How the Cash Book behaves offline
* How EOD works without connectivity
* How data synchronizes when connectivity returns
* How two devices recording transactions simultaneously are handled
* Conflict resolution — including conflicting changes to roles, shop assignments, or permissions
* Duplicate prevention (including duplicate client records created offline at different shops, per the open question raised during the Client/Worker/Service domain review)
* Sync status
* What happens when synchronization fails
* How multiple workers using different phones interact with the same Shop
* Which operations require an online connection, if any

This will be one of the most important architectural decisions for Ratibu.