package com.ratibu.client.domain;

import com.ratibu.common.domain.AggregateRoot;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Client — business-wide customer identity (ADR-004 §3–§7, ADR-012 §16,
 * ADR-013 §7). Never duplicated per shop; history is derived from activity.
 *
 * <p>Owned here:
 * <ul>
 *   <li>Client identity ({@link StableId})</li>
 *   <li>Owning Business reference (StableId only)</li>
 *   <li>Display name</li>
 *   <li>Optional phone (soft match key for manual merge queue — not unique)</li>
 *   <li>Optional email and notes</li>
 *   <li>Lifecycle status (ACTIVE / INACTIVE)</li>
 * </ul>
 *
 * <p>Not owned here: bookings, payments, visit history (derived), merge workflow.
 */
public final class Client extends AggregateRoot {

    private final StableId businessId;
    private ClientName name;
    private PhoneNumber phone;
    private String email;
    private String notes;
    private ClientStatus status;
    private final Instant createdAt;

    private Client(
            StableId id,
            StableId businessId,
            ClientName name,
            PhoneNumber phone,
            String email,
            String notes,
            ClientStatus status,
            Instant createdAt) {
        super(id);
        this.businessId = Objects.requireNonNull(businessId);
        this.name = Objects.requireNonNull(name);
        this.phone = phone;
        this.email = email;
        this.notes = notes;
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Client create(
            StableId businessId,
            ClientName name,
            PhoneNumber phone,
            String email,
            String notes) {
        return create(StableId.generate(), businessId, name, phone, email, notes, Instant.now());
    }

    public static Client create(
            StableId id,
            StableId businessId,
            ClientName name,
            PhoneNumber phone,
            String email,
            String notes,
            Instant createdAt) {
        if (id == null) {
            throw new InvariantViolationException("Client id must not be null");
        }
        if (businessId == null) {
            throw new InvariantViolationException("Client businessId must not be null");
        }
        if (name == null) {
            throw new InvariantViolationException("Client name must not be null");
        }
        String normalizedEmail = normalizeOptional(email, 254, "Client email");
        String normalizedNotes = normalizeOptional(notes, 2000, "Client notes");
        Client client = new Client(
                id, businessId, name, phone, normalizedEmail, normalizedNotes,
                ClientStatus.ACTIVE, createdAt);
        client.raise(new ClientCreated(id, businessId, name, phone, createdAt));
        return client;
    }

    public static Client reconstitute(
            StableId id,
            StableId businessId,
            ClientName name,
            PhoneNumber phone,
            String email,
            String notes,
            ClientStatus status,
            Instant createdAt) {
        return new Client(id, businessId, name, phone, email, notes, status, createdAt);
    }

    private static String normalizeOptional(String raw, int maxLength, String label) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new InvariantViolationException(label + " must be at most " + maxLength + " characters");
        }
        return trimmed;
    }

    public StableId getBusinessId() {
        return businessId;
    }

    public ClientName getName() {
        return name;
    }

    public Optional<PhoneNumber> getPhone() {
        return Optional.ofNullable(phone);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getNotes() {
        return Optional.ofNullable(notes);
    }

    public ClientStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return status == ClientStatus.ACTIVE;
    }

    public void rename(ClientName newName) {
        if (newName == null) {
            throw new InvariantViolationException("Client name must not be null");
        }
        if (status == ClientStatus.INACTIVE) {
            throw new InvariantViolationException("Cannot rename an inactive client");
        }
        this.name = newName;
    }

    public void updatePhone(PhoneNumber newPhone) {
        if (status == ClientStatus.INACTIVE) {
            throw new InvariantViolationException("Cannot update phone on an inactive client");
        }
        this.phone = newPhone;
    }

    public void updateContact(String email, String notes) {
        if (status == ClientStatus.INACTIVE) {
            throw new InvariantViolationException("Cannot update contact on an inactive client");
        }
        this.email = normalizeOptional(email, 254, "Client email");
        this.notes = normalizeOptional(notes, 2000, "Client notes");
    }

    /** Soft-deactivate. Historical bookings and payments remain (ADR-004 §7). */
    public void deactivate() {
        if (status == ClientStatus.INACTIVE) {
            return;
        }
        this.status = ClientStatus.INACTIVE;
    }

    public void activate() {
        if (status == ClientStatus.ACTIVE) {
            return;
        }
        this.status = ClientStatus.ACTIVE;
    }
}
