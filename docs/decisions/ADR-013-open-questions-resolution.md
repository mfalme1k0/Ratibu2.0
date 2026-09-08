# ADR-013: Resolution of Open Cross-ADR Questions

**Status:** Accepted
**Date:** 2026-09-08
**Decision Type:** Domain / Product
**Project:** Ratibu
**Context:** This ADR closes out the open questions identified during a consolidated review of ADR-001 through ADR-012. Each decision below amends or completes the ADR where the question originally arose. This ADR does not introduce new domain concepts on its own — it resolves forks that earlier ADRs deliberately or inadvertently left open.

---

## 1. EOD Reconciliation Conflict Resolution

**Amends:** ADR-006, ADR-010 §37, ADR-011 §33–35, ADR-012 §14/§16

**Decision:** EOD Reconciliation is unique per (Shop, Business Date), but not enforced as a hard single-submission constraint. When two devices submit EOD reconciliations for the same Shop and Business Date — typically because both were offline — the **second reconciliation to sync supersedes the first as the shop's official record for that day**. Both submissions are retained in history; neither is deleted or silently overwritten. The system flags the discrepancy between the two submissions (different actual-cash counts, different users, different times) for the Owner or Manager to review.

**Rationale:** This avoids blocking shop operation on network coordination (rejected: "require online coordination"), and avoids forcing a business rule ("first wins") onto what is actually a factual disagreement about physical cash that only a human can resolve. Superseding-with-flag keeps the audit trail intact while giving the business a clear current answer.

**Rule (added to ADR-011/012's Cash/EOD aggregate):**
> An EOD Reconciliation aggregate is keyed by (Shop ID, Business Date). A new reconciliation for an already-reconciled (Shop, Business Date) does not fail — it is accepted, marked current, and both it and the superseded reconciliation remain queryable. A `discrepancy_between_reconciliations` flag is raised whenever a second reconciliation differs from the first for the same key.

---

## 2. Business-Level (Non-Shop) Financial Events

**Amends:** ADR-003, ADR-006 Rule 5, ADR-011

**Decision:** Ratibu introduces a genuine **Business-level financial event type**, distinct from Shop-level financial events. Not every financial event must belong to a Shop — an event may instead belong directly to the Business.

**Rationale:** Rejected forcing every cost onto an arbitrary "head office" shop, since that would corrupt shop-level financial reporting (a business subscription cost showing up as a Westlands expense misrepresents Westlands' actual performance). A real category is needed.

**Rule (amends ADR-006 Rule 5 and ADR-003 Rule 4):**
> A financial event belongs to exactly one of: a Shop, or the Business directly. Business-level events (e.g., a business-wide subscription, an owner's general cash injection not tied to any one shop's till) are excluded from Shop-level cash books and Shop-level EOD, but are included in Business-level financial aggregation and insights (ADR-001 §11, ADR-003 §9–10 are amended accordingly: business totals are the sum of Shop-level activity *plus* Business-level activity, not purely derived from Shops).

---

## 3. Overpayment

**Amends:** ADR-011 §8

**Decision:** Overpayment is allowed. The excess amount is recorded as an **unexplained overpayment**, visible on the relevant booking/transaction and surfaced for manual reconciliation by an authorized user. No client-credit balance concept is introduced in MVP.

**Rule (added to ADR-011):**
> A Payment may exceed the amount due. The excess is stored as `overpayment_amount` on the Payment record and does not automatically adjust any other booking, client balance, or future amount due. It appears in a reconciliation/review view until an authorized user acknowledges or explains it.

---

## 4. Worker-Service Capability Enforcement

**Amends:** ADR-004 §13, ADR-005 §9, ADR-012 (Booking Aggregate rules)

**Decision:** Hard enforcement. A booking cannot be created (or a worker assigned) for a service the worker is not recorded as capable of performing.

**Rule (added to Booking aggregate, ADR-012 §16):**
> Booking creation/worker-assignment validates that the assigned Worker's recorded service capabilities (ADR-004 §13) include the Booking's Service. If not, the operation is rejected. Changing a worker's capabilities is a separate, explicit action (e.g., an Owner/Manager adding "Coloring" to a worker's profile) — there is no override-at-booking-time escape hatch in MVP.

**Note:** Because this is a hard block, the Worker-Service capability list (ADR-004 §13) becomes a harder dependency than before — businesses must configure it accurately, or workers will be unable to be booked for services they actually do perform. Worth flagging as an onboarding UX consideration: the default worker capability list should probably start as "all business services" and be narrowed, not start empty and require opt-in per service, or new workers will be unbookable for everything until configured.

---

## 5. Bookings and Multiple Services

**Amends:** ADR-005 §4

**Decision:** One Service per Booking for MVP. Multi-service/bundled bookings are explicitly out of scope and deferred to a future ADR.

**Rule (added to ADR-005):**
> A Booking references exactly one Service. A client requiring multiple services in one visit is represented as multiple Bookings (e.g., sequential or concurrent), not as one Booking with multiple line items, until a future ADR revisits this.

---

## 6. Final Price vs. Expected (Booked) Price

**Amends:** ADR-005 §8, ADR-011 §5

**Decision:** A distinct **Final Price** concept is introduced, separate from the Booking's Expected Price captured at booking time (ADR-005 §8). Divergence between the two — a discount, upsell, or on-the-spot negotiation — is explicit and visible, not inferred from the payment total.

**Rule (added to ADR-005/ADR-011):**
> A Booking captures `expected_price` at creation time (unchanged from ADR-005 §8). At service completion, an authorized user may set a `final_price`, defaulting to `expected_price` if not explicitly changed. `Amount Due` for payment purposes is the `final_price`, not the `expected_price`. Any difference between `expected_price` and `final_price` is visible on the booking record and available to insights (e.g., "discounting frequency," "average discount given") without requiring it to be inferred from a payment/expected-price mismatch.

---

## 7. Duplicate Detection

**Amends:** ADR-004 §4, ADR-010 §40, ADR-011 Rule 26

**Decision:**
- **Clients:** Phone number is used as a soft matching key. Potential duplicate clients (same or very similar phone number, created independently, typically across shops or offline devices) are surfaced to an Owner or Manager as a **manual merge queue**. Ratibu never auto-merges client records.
- **Financial transactions:** Never matched or merged heuristically (not by amount, client, timestamp, or any combination). Identity is determined **solely** by the transaction's locally-generated stable ID (ADR-010 §14). Two transactions with identical amount/client/time are treated as two distinct real transactions unless they share the same ID.

**Rationale:** Auto-merging clients risks incorrectly combining two different people; auto-merging financial transactions risks silently discarding real money. Both err toward showing a human the ambiguity rather than resolving it silently — consistent with the EOD and overpayment decisions above.

---

## 8. Housekeeping Corrections (non-substantive, applied for consistency)

1. The document currently titled "ADR-008 Amendment: Owner as a Worker / Solo-Shop Business" is retitled **ADR-009: Owner as Worker and Solo-Shop Business Model**. Its Rules 18–24 are renumbered as a continuation of ADR-008's rule list (Rules 20–26).
2. ADR-011 is prefixed with a note that it supersedes and extends the financial model introduced in ADR-006.
3. ADR-012 §16's Booking Aggregate state list is corrected to match ADR-005 §10 (`SCHEDULED → IN_PROGRESS → COMPLETED`, branching to `CANCELLED` / `NO_SHOW`), replacing the inconsistent `PENDING/CONFIRMED/COMPLETED/CANCELLED` list.
4. ADR-012 §17's aggregate diagram relabels `USERS` as `BUSINESS MEMBERSHIPS`, consistent with ADR-002's model (User Accounts are global; Business Memberships are Business-scoped).
5. ADR-012 §11 is reworded so "Financial Transaction" is described as a conceptual category (Payment, Expense, Refund, Cash Adjustment, and now the new Business-level event type from §2 above), not a candidate unified aggregate — matching §16's treatment of Payment and Expense as separate aggregates.
6. ADR-009 §6/§17 are corrected so that **Worker Profile is an optional attachment to a Business Membership** (not a peer Role alongside Owner/Manager, and not attached to the raw User Account). A person with Memberships in two different Businesses has independent Worker Profiles/capabilities in each.

---

## 9. Consequences

### Positive
- Every open question raised across the review of ADR-001–012 now has a stated resolution; no fork is left to be decided ad hoc during implementation.
- The resolutions are internally consistent with each other and with the set's existing historical-integrity and "surface ambiguity, don't silently resolve it" principles.
- The domain model (ADR-012) can now be finalized without unresolved dependencies.

### Negative
- Two new concepts are introduced (Business-level financial event type; Final Price distinct from Expected Price) that will need to be reflected in the domain model and any implementation-facing ADRs that follow.
- The hard worker-service capability block (§4) increases onboarding friction unless the default-capability UX note is followed.

---

## 10. Next Decision

With the domain and product model now fully resolved, the next ADR(s) move into **technical/Android architecture**: local database technology, the concrete sync engine design (building on ADR-010's principles), identifier generation strategy specifics, and the module/package structure implementing the aggregate boundaries from ADR-012.