package com.ratibu.common.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base type for every aggregate root in the system (Business, Shop, Booking,
 * Payment, and so on - see ADR-012 section 16 for the full aggregate list).
 *
 * <p>Aggregates communicate with each other only through their StableId and
 * through application services - never by directly reaching into another
 * aggregate's internal state (ADR-012 section 19). Domain events raised here
 * are the mechanism for that: an application service reacts to an event
 * rather than one aggregate calling another's methods directly.
 */
public abstract class AggregateRoot {

    private final StableId id;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(StableId id) {
        this.id = id;
    }

    public StableId getId() {
        return id;
    }

    protected void raise(DomainEvent event) {
        domainEvents.add(event);
    }

    /** Drains and returns the events raised since the last call. */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}