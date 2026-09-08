# Ratibu ADR Set — Consolidated Review

**Reviewed:** ADR-001 through ADR-012 (12 documents, including the ADR-008 Amendment / ADR-009)
**Purpose:** One place to see the full map, the housekeeping fixes needed, and the open questions that keep resurfacing so they get decided once — not seven times, differently, by whoever implements each piece.

---

## 1. The ADR Map

| # | Title | Owns |
|---|---|---|
| 001 | Product Vision and Core Business Model | Overall vision, operations→money→insights, offline-first commitment, MVP scope |
| 002 | Accounts, Business Memberships, Roles and Access Control | User Account, Business Membership, Worker Profile vs. Account, the 3 roles, no-Cashier decision |
| 003 | Business and Shop Organizational Structure | Business vs Shop boundary, the 10 structural rules, shop lifecycle |
| 004 | Clients, Workers and Services Domain Model | Client dedup, worker-shop assignment, worker-service capability, price snapshotting |
| 005 | Booking Lifecycle and Operational Events | Booking states, booking ≠ payment, offline sales as parallel path |
| 006 | Payments, Cash Book and EOD Reconciliation | First financial model: transaction-level cash book, EOD as reconciliation not re-entry |
| 007 | Business Insights and Analytics Model | Insight levels, metric definitions, dashboard philosophy |
| 008 | Permission Model, Access Scoping and Role-Based Experience *(revised)* | Concrete permission catalog, scope, UX/navigation by role — explicitly built on 002 |
| 009 | Owner as Worker / Solo-Shop Business *(currently titled "ADR-008 Amendment")* | Roles as capabilities not exclusive identities; Owner-as-Worker |
| 010 | Offline-First Architecture and Data Synchronization | Local-first principle, stable IDs, sync states, conflict philosophy |
| 011 | Cash Book and Financial Transaction Model | Deeper financial model: overpayment, owner cash movements, currency, decimal precision |
| 012 | Domain Model and Aggregate Boundaries | Aggregate map, where business rules live, aggregate communication |

---

## 2. Housekeeping — fix these before the set is "done"

1. **Title/number the "ADR-008 Amendment" document as ADR-009.** ADR-012 §5 already cites it as "ADR-009." Right now the title says ADR-008 Amendment, so the cross-reference doesn't resolve for a reader going by title alone.

2. **State the ADR-006 → ADR-011 relationship explicitly.** ADR-011 re-derives Service Value/Payment/Cash Book/EOD from first principles without citing ADR-006, even though it adds real new material (overpayment, owner cash injection/withdrawal, currency, decimal precision, transaction attribution). Add a line at the top of ADR-011: *"This ADR supersedes and extends the financial model introduced in ADR-006 with the detail needed for implementation."* Otherwise a future reader has two documents that both claim to define "what is a Cash Book" and no signal which one wins on a conflict.

3. **Reconcile the booking-state lists.** ADR-005 §10: `SCHEDULED → IN_PROGRESS → COMPLETED`, branching to `CANCELLED` / `NO_SHOW`. ADR-012 §16 (Booking Aggregate): `PENDING → CONFIRMED → COMPLETED → CANCELLED`. These aren't just renamed — `IN_PROGRESS` and `NO_SHOW` are dropped, and `PENDING`/`CONFIRMED` are new. Either ADR-012's list was illustrative shorthand (say so), or the lifecycle genuinely changed (in which case ADR-005 needs a short amendment, the same way ADR-008 got one).

4. **Clarify "Users" vs. "Business Memberships" in ADR-012 §17's aggregate diagram.** It shows `USERS` as a direct child aggregate of `BUSINESS`. Per ADR-002, the User Account is global (can span multiple Businesses); what's actually scoped to a Business is the **Business Membership**. Worth relabeling the diagram node so "deleting/scoping a Business" doesn't read as cascading to User Accounts themselves.

5. **Untangle ADR-012 §11 vs §16 on Financial Transaction.** §11 introduces "Financial Transaction" as if it might be one unified concept/table; §16 then lists Payment and Expense as separate aggregates. Pick one framing — probably: Financial Transaction is a *conceptual category*, not an aggregate itself; Payment, Expense, Refund, and Cash Adjustment are the actual aggregates.

6. **ADR-009's role model needs one more pass** before merging into ADR-002/008 (flagged at the time, repeating here so it doesn't get lost): decide whether Worker is a peer Role alongside Owner/Manager, or an independent capability/profile attachable regardless of Role (ADR-009 §6 implies the latter, §17's diagram implies the former — pick one and fix the diagram). Also: Worker Profile should attach to the **Business Membership**, not the raw User, so a person working at two unrelated businesses doesn't have their worker capabilities conflated across them.

7. **Renumber ADR-009's Rules 18–24** as a contiguous continuation of ADR-008's rule list (currently up to Rule 19 after the revision) once the two are actually merged, rather than restarting at 18.

---

## 3. Recurring open questions — decide these once

These same few questions have come up under five or six different ADRs. Each is listed with every place it's surfaced, so you can see it's not a new problem each time — it's the same handful of hard calls the whole set is dancing around.

### 3.1 EOD conflict resolution (highest priority — the most-deferred, least-resolved item in the set)
- ADR-006 §20: deferred to "financial workflow implementation."
- ADR-010 §37: explicitly says "these rules will be defined in the Cash Book/EOD ADR" (pointing at 011).
- ADR-011 §33–35: restates expected-vs-actual, doesn't address concurrent conflicting submissions.
- ADR-012 §14/§16: calls out EOD as needing "its own boundary... auditable," still doesn't resolve it.

**What's needed:** a concrete rule for what happens when two devices (e.g., two Managers) submit different EOD reconciliations for the same Shop + Business Date while both were offline. At minimum, an aggregate-level invariant: *EOD Reconciliation is unique per (Shop, Business Date); a second submission is either rejected outright, or accepted as a re-reconciliation that supersedes the first with both preserved in history and the discrepancy between the two flagged for review.* This isn't a "later" problem — it's foundational to how the EOD aggregate is built.

### 3.2 Duplicate detection (clients, and now financial transactions)
- ADR-004 §4: client dedup stated as a goal, mechanism unspecified.
- ADR-010 §40: acknowledges the offline multi-device duplicate-client risk, defers "the exact duplicate-resolution strategy."
- ADR-011 Rule 26: "no silent merging" for financial transactions, but doesn't say how a true duplicate is told apart from two genuinely distinct KSh 500 cash payments at the same minute.

**What's needed:** at least a stated default — e.g., phone number as a soft natural key for clients, surfaced to Owner/Manager as a manual merge queue rather than auto-merged; and for financial transactions, rely on the locally-generated stable ID (ADR-010 §14) as the sole determinant of "same transaction," with no heuristic matching on amount/time/client. Worth writing this down explicitly so it isn't decided ad hoc during implementation.

### 3.3 Business-level (non-shop) financial events
- ADR-003 flagged this in review; ADR-006 Rule 5 restates "every financial event belongs to a Shop" with no exception; ADR-011 doesn't revisit it either, despite introducing owner cash injection/withdrawal, which arguably *is* sometimes business-wide (e.g., an owner funding the business generally, not one specific shop).

**What's needed:** a decision — either a shop-less business-level event type exists, or every such cost gets assigned to a designated "primary" or "head office" shop by convention. This has been open since the third document reviewed and still isn't resolved anywhere in the set.

### 3.4 Worker-service capability: hard rule or soft signal?
- Raised at ADR-004 §13, raised again at ADR-005 §9 — still not answered anywhere, including ADR-012's aggregate rules for Booking.

**What's needed:** one sentence — is assigning an unqualified worker to a service blocked, warned-and-overridable, or left entirely to business judgment with no system enforcement?

### 3.5 Overpayment handling
- ADR-011 §8 names the fork (client credit vs. rejection) but explicitly declines to choose.

**What's needed:** pick one for MVP. Credit requires a client-level balance concept that doesn't exist yet in the Client domain (ADR-004); rejection is simpler to ship first.

### 3.6 Final price vs. expected/booked price
- Raised at ADR-005 review, never directly answered: if the actual amount charged at completion differs from the booking's expected price (discount, upsell, on-the-spot negotiation), is there a distinct "final price" field, or does the payment total simply diverge from the booking's expected price with no reconciliation flag?

### 3.7 One service per booking, or multi-service/bundled bookings?
- Raised at ADR-005 review, unaddressed since. Affects the payment model directly (a bundled booking needs a total derived from line items, not one flat price) and is expensive to retrofit later.

---

## 4. Smaller items worth a decision, not just a note

- **Post-completion Shop/Worker immutability** (ADR-005 §27–28) uses soft "should" language where the rest of the set uses hard "must" — tighten to match Rule-level force given how much the insight model depends on it.
- **Double-booking / worker time-conflict validation** — not mentioned anywhere; confirm it's intentionally deferred to a future scheduling ADR rather than assumed solved.
- **IN_PROGRESS → CANCELLED transition** missing from ADR-005's lifecycle diagram (client walks out mid-service is a real case).
- **Insight-visibility permissions** — ADR-007 §46 originally pointed at what became ADR-008; confirm ADR-008 §28 (Business Insights Permissions) is understood to be the actual resolution, since ADR-007 was written before ADR-008 existed in its final form.
- **Owner cash injection/withdrawal authorization** (ADR-011 §25–26) — who's allowed to do this? Given it moves cash without corresponding to a service or expense, probably Owner-only or Owner + explicitly delegated Manager, even in shops where Workers can otherwise record cash. Not stated anywhere.
- **Device Identity in transaction attribution** — ADR-010 §42 introduces it, ADR-011 §42's attribution structure doesn't include it. Worth adding, since financial transactions are exactly where conflict/sync diagnostics matter most.
- **Rejected-on-conflict operations representing real-world events already performed** (ADR-010 §27) — a plain "rejected" state isn't sufficient when the underlying haircut *did happen*; needs a resolution workflow, not just a rejection status.

---

## 5. What's genuinely solid — no notes needed

Worth saying plainly: the historical-integrity principle (prices, shop attribution, worker attribution, service configuration all snapshot at time of transaction rather than live-referencing current config) is applied *consistently* across ADR-003 through ADR-012. That's the hardest kind of discipline to maintain across a growing document set, and it hasn't slipped once. Same for the booking≠payment≠cash separation — it's stated, restated, and never contradicted anywhere in twelve documents. That's a strong foundation to build the remaining decisions on.

---

## 6. Suggested order of remaining work

1. Fix the housekeeping items in §2 (quick, mechanical).
2. Resolve §3.1 (EOD conflict) and §3.3 (business-level financial events) — these block finalizing the Cash/EOD aggregate design in ADR-012.
3. Resolve §3.2, §3.4, §3.5, §3.6, §3.7 — smaller, but each is a real fork that changes the data model if decided late.
4. Only then move to Android/technical architecture ADRs (local DB technology, sync engine implementation, identifier strategy specifics) — those should be downstream of the domain being fully settled, not concurrent with it.