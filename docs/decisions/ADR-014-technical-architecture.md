# ADR-014: Technical Architecture — Spring Boot Backend, Android and PWA Clients

**Status:** Proposed
**Date:** 2026-09-08
**Decision Type:** Technical Architecture
**Project:** Ratibu
**Amends:** ADR-001 §1 (which originally scoped Ratibu as a single Java Android application; this ADR supersedes that framing with a backend + multi-client model)

---

# 1. Context

ADR-001 through ADR-013 define Ratibu's product and domain model, deliberately deferring the technical/synchronization architecture (ADR-001 §17: "the synchronization architecture will be designed separately"; ADR-010 established the *principles* of offline-first sync without committing to a specific backend or client technology).

The project will now be built as:

* A **Spring Boot backend**, implementing the domain model from ADR-012 as the authoritative shared source of truth (per ADR-010 §43).
* An **Android client**, the primary offline-first operational tool — used on the shop floor by owners and workers, where connectivity is least reliable.
* A **Progressive Web App (PWA)** client — a browser-based surface, likely used more by owners/managers for review, dashboards, and less time-critical operational tasks.

This is a real scope expansion beyond ADR-001's original single-Android-app framing, and introduces a question ADR-010 didn't need to answer when only one client existed: **do both clients get the same offline-first guarantees, or different ones?**

---

# 2. Problem

ADR-010's offline-first model assumes a client with:

* A durable local database
* A reliable mechanism to queue writes and flush them automatically when connectivity returns

Android (via Room + a background sync service) can deliver this reliably.

A browser-based PWA cannot deliver the same guarantee with equivalent reliability. Specifically:

* The Background Sync API, which allows a queued write to flush automatically without the page being open, has inconsistent browser support — notably weak or absent on iOS Safari.
* Browser storage (IndexedDB) can be evicted under storage pressure without the same durability guarantees as a native app's local database.

Given ADR-011's emphasis on financial data never being silently lost or duplicated, treating the PWA as a full offline-first peer to Android would mean building financial-integrity guarantees on top of an unreliable foundation.

---

# 3. Decision

Ratibu will use a **single backend, two clients with different offline capability tiers**:

```text
                    SPRING BOOT BACKEND
                    (authoritative domain model, ADR-012)
                            │
              ┌─────────────┴─────────────┐
              │                           │
        ANDROID CLIENT               PWA CLIENT
     (full offline-first,          (offline reads,
      per ADR-010)                  online-required writes,
                                     with local draft holding)
```

Both clients speak to the **same sync/API contract**. The difference is in each client's write behavior when offline, not in the domain model or API shape.

---

# 4. Backend (Spring Boot)

## 4.1 Responsibilities

The backend is the authoritative implementation of the aggregates defined in ADR-012:

* Business, Shop, Business Membership (ADR-002, ADR-008)
* Client, Worker Profile, Service (ADR-004)
* Booking (ADR-005, with the state-model correction from ADR-013 §8.3)
* Payment, Expense, Business-level Financial Event (ADR-006, ADR-011, ADR-013 §2)
* Cash Book, EOD Reconciliation (ADR-006, ADR-011, ADR-013 §1)
* Insights (ADR-007), computed server-side from the above, never stored as authoritative state (ADR-012 §15)

## 4.2 API Shape

* A REST API for standard CRUD/query operations, scoped per Business Membership and Shop Assignment (ADR-008's permission/scope model enforced server-side, not just in client UI).
* A dedicated **sync endpoint** (or small set of endpoints) implementing ADR-010's model: clients submit locally-generated, stable-ID-tagged operations; the server validates, applies domain invariants (ADR-012 §18), and returns per-operation acceptance/rejection/conflict status.
* Authentication via JWT (or equivalent), carrying the authenticated User Account; Business Membership and permission resolution happens server-side per request.

## 4.3 Persistence

* PostgreSQL, chosen for strong support of exact decimal arithmetic (ADR-011 §50 — `NUMERIC` type) and mature transactional guarantees needed for the EOD/financial invariants in ADR-013 §1.
* Financial and audit-relevant tables are append-preferring per ADR-011 §41/ADR-012 §21 — corrections and reversals as new rows, not destructive updates.

## 4.4 Multi-Tenancy

* Business is the tenant boundary (ADR-003 §4, ADR-008 §4). Every query is scoped by Business Membership; no cross-Business data access is possible even via a bug in a single query — this should be enforced at a shared data-access layer, not re-implemented per endpoint.

---

# 5. Android Client

* Local database: Room, mirroring the aggregate boundaries from ADR-012.
* Full offline-first per ADR-010: local-first reads and writes, locally-generated stable IDs (ADR-010 §14), automatic background sync when connectivity returns, sync status visible to the user (ADR-010 §9, §30).
* Conflict handling per ADR-010 §23 and the specific resolutions in ADR-013 (EOD superseding-with-flag, worker-service hard block, etc.).
* This remains the primary client for real-time, shop-floor operational use — booking, service completion, payment recording, offline sales, EOD — consistent with ADR-001's original target use case.

---

# 6. PWA Client

## 6.1 Offline Capability Tier

The PWA uses a **lighter offline tier** than Android:

* **Reads work offline.** Dashboards, cash book history, today's bookings, client lists, and insights are cached locally (via a service worker + IndexedDB or similar) and remain viewable without connectivity.
* **Writes require connectivity to submit**, but are never silently blocked or lost. If a user attempts a write (recording a payment, creating a booking, etc.) while offline, the PWA holds it as a **visible local draft** — clearly marked as "not yet submitted, connect to send" — rather than either failing outright or attempting an unreliable background sync.
* The PWA does **not** rely on the Background Sync API or any other mechanism assuming automatic flush-when-reconnected without the tab being open. A draft is submitted when the user is online and either does so explicitly or the app auto-attempts submission while the tab is open and connectivity is detected.

## 6.2 Rationale

This avoids building financial-integrity guarantees (ADR-011, ADR-013 §1/§3) on top of browser APIs — particularly Background Sync — that don't reliably support them across browsers, notably iOS Safari. It also fits the expected usage pattern: the PWA is more likely used by an Owner or Manager reviewing performance or handling less time-critical tasks from a laptop or browser, not a worker recording a payment mid-service with no signal — that scenario is Android's job.

## 6.3 Explicit Non-Goal

Full offline-first write parity with Android is explicitly **not** a goal for the PWA in the initial architecture. If real usage patterns later show PWA users need offline writes as urgently as Android users (e.g., PWA becomes a primary shop-floor tool in some markets), this should be revisited as its own ADR rather than retrofitted quietly — full parity would likely require either accepting the reliability gap or a materially more complex client (e.g., a wrapped/hybrid app rather than a pure PWA).

---

# 7. Shared Sync Contract

Both clients use the same backend sync semantics:

* Locally-generated stable IDs (UUIDs) for all synchronizable entities, created before any server contact (ADR-010 §14).
* Idempotent submission — resubmitting an already-accepted operation has no additional effect (ADR-010 §13).
* Server-side validation is authoritative; local/client-side validation is a UX convenience only (ADR-010 §25).
* Conflict resolution rules are defined per-aggregate at the backend, not per-client — so Android and PWA cannot diverge in how a conflict is resolved, only in how eagerly they attempt writes while offline.

---

# 8. Consequences

## Positive
- A single backend and domain implementation serves both clients — no duplicated business logic.
- Android gets the full offline-first experience the product vision (ADR-001) requires for shop-floor use.
- The PWA avoids taking on financial-integrity risk it can't reliably back up technically.
- The tiered approach is honest about browser platform limitations rather than overpromising offline parity and quietly under-delivering.

## Negative
- Two clients now have genuinely different capability tiers, which needs to be clearly communicated to users (a PWA user should understand *why* they can't record an offline payment, not just hit a confusing error).
- The backend must serve two different client sync patterns (full offline-first queue vs. draft-and-submit-when-online) rather than one uniform pattern.
- If PWA usage patterns shift toward primary shop-floor use, this architecture will need revisiting.

---

# 9. Next Decisions

Before implementation begins, the following should be specified:

* Concrete API contract (endpoint list, request/response shapes) for the sync mechanism and standard CRUD operations.
* Database schema translating ADR-012's aggregates into PostgreSQL tables, including the append-preferring financial tables.
* Android module structure mirroring the aggregate boundaries.
* PWA technology choice (framework, service worker strategy, local storage approach).
* Authentication/session implementation details (JWT issuance, refresh, Business Membership resolution).