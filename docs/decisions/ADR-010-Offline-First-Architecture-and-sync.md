# ADR-010: Offline-First Architecture and Data Synchronization

**Status:** Proposed
**Date:** 2026-09-01
**Decision Type:** Architecture
**Project:** Ratibu

---

# 1. Context

Ratibu is intended for small businesses where reliable internet connectivity cannot always be assumed.

A business may need to operate when:

* Mobile data is unavailable
* Wi-Fi is unavailable
* Internet connectivity is slow
* The device temporarily loses connection
* A Shop has intermittent connectivity
* Multiple workers are using different devices

Business operations should not stop simply because the internet is unavailable.

This is particularly important because Ratibu records operational and financial events such as:

* Bookings
* Client information
* Services
* Payments
* Offline sales
* Expenses
* Cash-book transactions
* EOD reconciliation

These operations may need to happen immediately.

---

# 2. Problem

A traditional online-first application might work like:

```text
User
 ↓
Internet
 ↓
Server
 ↓
Database
 ↓
Response
```

If the internet disappears:

```text
User
 ↓
X Internet
 ↓
Operation fails
```

This would be problematic for Ratibu.

For example, a worker should be able to record:

```text
Client paid KSh 500 cash
```

even when there is no internet connection.

Similarly, a solo owner should be able to:

* View today's bookings
* Complete a service
* Record a payment
* Record an expense
* Update the Cash Book
* Perform relevant business operations

while offline.

---

# 3. Decision

Ratibu will use an **offline-first architecture**.

The local device will be treated as a first-class data store rather than merely a temporary cache.

Conceptually:

```text id="1c4m8v"
             RATIBU ANDROID APP
                     │
              LOCAL DATABASE
                     │
             LOCAL OPERATIONS
                     │
             SYNC ENGINE
                     │
               INTERNET
                     │
                SERVER
                     │
             CENTRAL DATABASE
```

The application should remain useful when disconnected.

---

# 4. Core Principle

The primary principle is:

> If an operation can safely be performed offline, Ratibu should allow it to be performed offline.

The application should not unnecessarily block users because connectivity is unavailable.

---

# 5. Local Database

The Android application will maintain a local database containing the data required for offline operation.

The local database may contain:

* Businesses accessible to the user
* Shops accessible to the user
* Clients
* Services
* Bookings
* Payments
* Cash-book transactions
* Expenses
* Relevant worker information
* Synchronization metadata

The exact database technology will be decided in the Android architecture ADR.

---

# 6. Local-First Read Operations

Reads should primarily use local data.

For example:

```text id="j3n8mx"
User opens bookings
        ↓
Local database
        ↓
Bookings displayed
```

The application should not require an internet request simply to display previously synchronized bookings.

---

# 7. Local-First Write Operations

Writes should first be committed locally.

For example:

```text id="9f5k0r"
Worker records payment
        ↓
Validate locally
        ↓
Save locally
        ↓
Mark as pending synchronization
        ↓
Show success to user
        ↓
Synchronize later
```

The user should receive immediate confirmation that the local operation succeeded.

---

# 8. Synchronization

When connectivity becomes available:

```text id="y2n1oe"
Local Changes
     ↓
Sync Queue
     ↓
Server
     ↓
Central Database
```

The server acknowledges successfully synchronized operations.

The local record can then be marked as synchronized.

---

# 9. Synchronization Status

The application should communicate synchronization state clearly.

Possible states include:

```text id="t3q8lw"
SYNCED
PENDING
SYNCING
FAILED
CONFLICT
```

For example:

```text id="q3i8br"
Payment recorded
✓ Saved

Sync:
Pending
```

This is preferable to making the user think the transaction was lost.

---

# 10. Offline Does Not Mean Failed

The following should not happen:

```text id="6wq9t5"
No Internet
    ↓
"Payment failed"
```

if the payment was successfully saved locally.

Instead:

```text id="u7k1e2"
Payment recorded locally
    ↓
Waiting for synchronization
```

The distinction is important.

---

# 11. Connectivity

Ratibu should detect connectivity changes.

Conceptually:

```text id="7h0d6k"
OFFLINE
  ↓
Local Operations
  ↓
ONLINE
  ↓
Synchronization
```

Synchronization should begin automatically when appropriate.

The user should not need to manually export or re-enter transactions.

---

# 12. Sync Queue

Changes that have not reached the server will be maintained in a synchronization mechanism.

Conceptually:

```text id="a4t8nm"
┌─────────────────────┐
│ Local Change        │
├─────────────────────┤
│ Create Payment      │
│ Update Booking      │
│ Record Expense      │
│ Complete Service    │
└──────────┬──────────┘
           ↓
       SYNC QUEUE
           ↓
         SERVER
```

The implementation details of the queue will be defined later.

---

# 13. Idempotency

Synchronization must prevent the same operation from being applied multiple times.

For example:

```text id="3f2k6n"
Payment
KSh 500
```

must not become:

```text id="m9k7q2"
KSh 1,000
```

simply because the synchronization request was retried.

Every synchronizable operation must therefore have a stable identity.

---

# 14. Stable Identifiers

Ratibu will use globally unique identifiers for entities and synchronizable operations.

An identifier should be generated in a way that does not require communication with the server.

Conceptually:

```text id="r4m7w8"
Device
  ↓
Generate ID locally
  ↓
Create record
  ↓
Synchronize later
```

This allows multiple devices to create records while offline.

The exact identifier strategy will be finalized in the data-model ADR.

---

# 15. Eventual Consistency

Ratibu will accept eventual consistency for distributed data.

For example:

```text id="x2c5z7"
Worker's phone
    ↓
Payment recorded

        INTERNET UNAVAILABLE

Manager's phone
    ↓
Does not yet see payment
```

After synchronization:

```text id="e7n3p1"
Worker phone
      ↓
      SERVER
      ↓
Manager phone
```

The Manager's device eventually receives the new information.

This is an intentional consequence of offline-first operation.

---

# 16. Critical Financial Data

Financial transactions require additional care.

A locally recorded payment must not simply disappear if synchronization fails.

For example:

```text id="4j7b2k"
Payment
KSh 1,000
Status:
Pending Sync
```

The transaction remains part of the local financial record until synchronization succeeds or an explicit resolution occurs.

---

# 17. Financial Immutability

Financial records should generally be treated as immutable events.

Instead of:

```text id="k7w4q1"
Payment KSh 1,000
    ↓
Edit
    ↓
Payment KSh 500
```

Ratibu should prefer:

```text id="r8m3p5"
Original Payment
KSh 1,000

        ↓

Correction / Reversal
-KSh 500
```

This preserves the audit trail.

The exact financial correction model will be defined in the financial-domain ADR.

---

# 18. Offline Cash Book

The Cash Book must work offline.

A user should be able to record:

```text id="c2f6n8"
Cash In
KSh 500
```

or:

```text id="h7j3m1"
Cash Out
KSh 300
```

without an internet connection.

The transaction should immediately affect the locally calculated cash position.

---

# 19. Offline EOD

Where the business workflow permits it, EOD should also be usable offline.

For example:

```text id="p8q4s2"
Expected Cash
KSh 12,500

Actual Cash
KSh 12,500

Difference
KSh 0

EOD
RECONCILED
```

The reconciliation can later synchronize with the server.

---

# 20. EOD Synchronization

EOD records require special handling because they represent a business state at a particular point in time.

For example:

```text id="f3r8m5"
Shop
Westlands

Business Date
2026-09-01

Expected Cash
KSh 12,500

Actual Cash
KSh 12,500

Status
Reconciled
```

The server must preserve the fact that the EOD was performed and by whom.

---

# 21. Multiple Devices

A Shop may have multiple users operating on different devices.

For example:

```text id="k3v7b9"
                 SHOP
                  │
        ┌─────────┼─────────┐
        ↓         ↓         ↓
     Owner      Worker    Worker
     Phone      Phone     Phone
```

Each device may generate local changes.

The synchronization architecture must safely merge those changes.

---

# 22. Concurrent Changes

Multiple devices may modify related information.

For example:

```text id="e5x2q8"
Worker A:
Completes Booking #123

Worker B:
Cancels Booking #123
```

If both devices were offline, the server eventually receives conflicting operations.

Ratibu must therefore define conflict-resolution rules.

---

# 23. Conflict Resolution

Conflicts should not simply be resolved using:

```text id="5k7n1q"
"Last device to sync wins"
```

for every type of data.

Different entities require different strategies.

For example:

### Independent transactions

Usually merge safely.

```text id="2f8j4s"
Payment A
+
Payment B
=
Both retained
```

### Booking state

May require domain-specific validation.

```text id="1d7p9m"
Completed
vs
Cancelled
```

### Financial corrections

Should require stronger rules.

The exact conflict strategy will be defined per aggregate/entity in a later ADR.

---

# 24. Domain Invariants

Offline operation must not bypass business rules.

For example:

```text id="g8m2c6"
Cancelled Booking
    ↓
Cannot become
Completed Service
```

merely because the device is offline.

The same domain rules should apply locally and on the server wherever practical.

---

# 25. Server Validation

Local validation improves the user experience but does not replace server validation.

When synchronization occurs:

```text id="m4x7p2"
Local Operation
      ↓
Server Validation
      ↓
Accepted
   OR
Rejected / Conflict
```

This is necessary because the server may have received changes from another device.

---

# 26. Failed Synchronization

If synchronization fails because of connectivity:

```text id="t8q3n6"
Pending
   ↓
Sync Attempt
   ↓
Network Failure
   ↓
Remain Pending
```

The operation should be retried.

The application should not duplicate the operation during retries.

---

# 27. Permanent Synchronization Failure

If the server rejects an operation because it violates a rule:

```text id="u3k9f1"
Pending
   ↓
Server Rejects
   ↓
Conflict / Failed
```

The user should be informed appropriately.

For important financial or operational data, the user should be able to understand what happened and what action is required.

---

# 28. Sync Retry

Synchronization should use controlled retries.

Conceptually:

```text id="b8q2m4"
Attempt 1
   ↓
Failure
   ↓
Wait
   ↓
Attempt 2
   ↓
Failure
   ↓
Wait
   ↓
Attempt 3
```

The exact retry policy will be defined during implementation.

---

# 29. Sync Should Be Automatic

Normal users should not need to understand synchronization mechanics.

The expected experience is:

```text id="r7n3k5"
Record transaction
      ↓
Transaction saved
      ↓
Internet returns
      ↓
Automatically synchronized
```

Manual synchronization may be available for troubleshooting, but should not be required for ordinary use.

---

# 30. User Visibility

Synchronization status should be visible without becoming distracting.

For example:

```text id="j4m8p2"
☁ Synced
```

or:

```text id="h5k2n9"
☁ 3 changes waiting to sync
```

The user should be able to investigate synchronization problems when necessary.

---

# 31. Offline Data Ownership

Local data belongs to the Business context and the authenticated user's authorized access.

The application must avoid exposing another user's Business data simply because data exists on the device.

This becomes especially important when:

* Users log out
* Users change Businesses
* A device is shared
* A Worker is removed from a Shop

Security requirements will be addressed in the security ADR.

---

# 32. Local Data and Logout

Logging out should not automatically mean that all local business data is permanently deleted.

However, access to that data must be protected.

The exact strategy for:

* Encryption
* Session handling
* Local database isolation
* Device security
* Data removal

will be defined in the security architecture.

---

# 33. Multi-Business Users

A User may eventually belong to multiple Businesses.

For example:

```text id="e7m3p5"
User
 ├── Business A
 │     └── Worker
 │
 └── Business B
       └── Owner
```

Local data must therefore remain properly separated by Business.

A record from Business A must never appear in Business B.

---

# 34. Offline Insights

Insights should distinguish between synchronized and locally recorded data.

For example:

```text id="c8m4q2"
Today's Revenue

KSh 15,000

Includes:
✓ Synchronized transactions
✓ Local pending transactions
```

The user should understand that today's number may change after synchronization with other devices.

---

# 35. Multi-Device Insights

For a multi-worker Shop:

```text id="v4n7k1"
Worker A
   ↓
Local transaction

Worker B
   ↓
Local transaction

       ↓

   Synchronization

       ↓

    SERVER

       ↓

Consolidated Shop Insight
```

Until synchronization occurs, individual devices may temporarily display different numbers.

This is expected under eventual consistency.

---

# 36. Sync and EOD

EOD should make synchronization state visible.

For example:

```text id="z6m3p8"
EOD

Expected Cash:
KSh 25,000

Actual Cash:
KSh 25,000

Local Status:
Reconciled

Sync Status:
Pending
```

This prevents the user from confusing local completion with server synchronization.

---

# 37. Conflict Handling for EOD

EOD conflicts require special treatment.

For example:

```text id="w5j8r2"
Manager A
records EOD

Manager B
also records EOD
```

The system must not silently overwrite one reconciliation with another.

EOD should therefore have explicit domain rules determining:

* Who can perform it
* Whether multiple EOD records are allowed
* Whether an EOD can be reopened
* How corrections are made
* What happens when two devices attempt reconciliation

These rules will be defined in the Cash Book/EOD ADR.

---

# 38. Offline Booking

Bookings should be creatable offline where the required information is available.

For example:

```text id="n8f3q6"
Create Booking
    ↓
Select Client
    ↓
Select Service
    ↓
Select Time
    ↓
Save Locally
```

The booking receives a local identifier and synchronizes later.

---

# 39. Offline Client Creation

A Worker or Owner with permission should be able to create a client offline.

For example:

```text id="q4m7s2"
New Client
   ↓
Jane Doe
   ↓
Save
   ↓
Local Database
   ↓
Sync Later
```

The system must prevent duplicate clients during synchronization as far as reasonably possible.

---

# 40. Duplicate Prevention

Offline operation creates the possibility that two devices create records representing the same real-world entity.

For example:

```text id="v2c8m5"
Phone A:
Jane Doe
0712...

Phone B:
Jane Doe
0712...
```

The synchronization layer should support duplicate detection or domain-specific reconciliation.

The exact duplicate-resolution strategy will be defined separately.

---

# 41. Time

Offline systems require careful handling of time.

Transactions should capture appropriate timestamps.

For example:

```text id="k6p3r9"
Occurred At
2026-09-01 14:32

Recorded At
2026-09-01 14:32
```

If synchronization occurs later:

```text id="b4n7m2"
Occurred At
14:32

Synced At
18:47
```

These timestamps represent different events and should not be conflated.

---

# 42. Device Identity

The synchronization architecture should be able to identify the originating device where necessary.

Conceptually:

```text id="p5q8m3"
Business
Transaction
Device
User
Timestamp
```

This may assist with:

* Debugging
* Conflict resolution
* Auditability
* Synchronization diagnostics

The exact device identity strategy will be defined during implementation.

---

# 43. Server as Source of Shared Truth

The server will act as the authoritative shared source of truth for synchronized Business data.

However:

> The server is not required for ordinary offline operation.

The model is therefore:

```text id="n7m4x2"
LOCAL AUTHORITY
       +
SERVER AUTHORITY
       ↓
EVENTUAL CONSISTENCY
```

Local data is authoritative for the user's immediate offline experience, while the server provides the shared Business state once synchronization occurs.

---

# 44. Architecture Principle

Ratibu will follow:

> Local-first, synchronize-second.

Not:

> Online-first, cache-if-possible.

This distinction is fundamental.

---

# 45. Consequences

## Positive consequences

* Ratibu remains usable without internet.
* Transactions can be recorded immediately.
* Small businesses can continue operating during connectivity problems.
* Offline payments are supported naturally.
* Offline Cash Book operations are possible.
* Solo owners can operate without depending on connectivity.
* Multiple workers can use the system independently.
* Data eventually converges across devices.
* The architecture supports unreliable network environments.

## Negative consequences

* Synchronization is significantly more complex than a traditional online application.
* Conflict resolution is required.
* Duplicate prevention becomes important.
* Financial data requires stronger consistency rules.
* Local storage must be secured.
* Time and identifiers must be handled carefully.
* Testing must include offline, reconnection and multi-device scenarios.
* Insights may temporarily differ between devices.
* EOD requires specialized synchronization rules.

These costs are accepted because offline operation is a core requirement of Ratibu.

---

# 46. Core Domain/Architecture Rules

### Rule 1

> Ratibu is offline-first.

### Rule 2

> Reads should primarily use locally available data.

### Rule 3

> Writes should be persisted locally before synchronization.

### Rule 4

> Offline operations should not be treated as failures when they are successfully persisted locally.

### Rule 5

> Synchronization must be automatic where possible.

### Rule 6

> Synchronization must be idempotent.

### Rule 7

> Local identifiers must support offline creation.

### Rule 8

> Domain invariants must apply during offline operation.

### Rule 9

> The server must validate synchronized operations.

### Rule 10

> Financial transactions must remain durable until synchronization succeeds or an explicit resolution is made.

### Rule 11

> Financial history should favor immutable records and corrections over destructive edits.

### Rule 12

> Conflicts must be resolved according to the type of data rather than using a universal last-write-wins strategy.

### Rule 13

> Business data must remain isolated between Businesses.

### Rule 14

> Synchronization status must be visible to users.

### Rule 15

> Offline and synchronized data may temporarily produce different views across devices.

### Rule 16

> EOD requires explicit synchronization and conflict rules.

### Rule 17

> Timestamps must distinguish when an event occurred from when it was synchronized.

---

# 47. Next Decision

The next ADR should define the **Cash Book and Financial Transaction Model** in greater detail.

ADR-010 establishes that the Cash Book must work offline, but it does not yet define the financial domain deeply enough.

The next decision should establish:

* What constitutes a financial transaction
* Cash In
* Cash Out
* Booking payments
* Offline payments
* Expenses
* Refunds
* Corrections
* Reversals
* Opening balance
* Closing balance
* Expected cash
* Actual cash
* EOD
* Outstanding payments
* Partial payments
* Payment methods
* Mobile money
* Cash
* Card
* Credit
* Revenue vs cash received
* Shop-level financial records
* Business-level aggregation
* Financial audit trail
* How corrections work
* How financial transactions interact with bookings
* How financial transactions interact with synchronization

That ADR will give us the financial foundation needed before we start designing the actual Java domain model.
