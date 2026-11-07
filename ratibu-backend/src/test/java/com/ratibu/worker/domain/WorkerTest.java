package com.ratibu.worker.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkerTest {

    private final StableId businessId = StableId.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final WorkerName name = WorkerName.of("Mary Wanjiku");

    @Test
    void create_raisesWorkerCreated() {
        Worker w = Worker.create(businessId, name, null);

        assertEquals(name, w.getName());
        assertEquals(businessId, w.getBusinessId());
        assertTrue(w.getMembershipId().isEmpty());
        assertEquals(WorkerStatus.ACTIVE, w.getStatus());
        assertTrue(w.isActive());

        List<DomainEvent> events = w.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(WorkerCreated.class, events.getFirst());
    }

    @Test
    void create_withMembership_storesLink() {
        StableId membershipId = StableId.fromString("11111111-1111-1111-1111-111111111111");
        Worker w = Worker.create(businessId, name, membershipId);
        assertEquals(membershipId, w.getMembershipId().orElseThrow());
    }

    @Test
    void reconstitute_doesNotRaiseEvents() {
        Worker w = Worker.reconstitute(
                StableId.generate(), businessId, name, null, WorkerStatus.ACTIVE, Instant.now());
        assertTrue(w.pullDomainEvents().isEmpty());
    }

    @Test
    void rename_updatesName() {
        Worker w = Worker.create(businessId, name, null);
        w.pullDomainEvents();
        w.rename(WorkerName.of("Mary K."));
        assertEquals("Mary K.", w.getName().asString());
    }

    @Test
    void rename_rejectsInactive() {
        Worker w = Worker.create(businessId, name, null);
        w.deactivate();
        assertThrows(InvariantViolationException.class,
                () -> w.rename(WorkerName.of("Other")));
    }

    @Test
    void deactivate_isIdempotent() {
        Worker w = Worker.create(businessId, name, null);
        w.deactivate();
        w.deactivate();
        assertEquals(WorkerStatus.INACTIVE, w.getStatus());
        assertFalse(w.isActive());
    }

    @Test
    void activate_restoresActive() {
        Worker w = Worker.create(businessId, name, null);
        w.deactivate();
        w.activate();
        assertTrue(w.isActive());
    }

    @Test
    void create_rejectsNullName() {
        assertThrows(InvariantViolationException.class,
                () -> Worker.create(businessId, null, null));
    }

    @Test
    void workerName_rejectsBlank() {
        assertThrows(InvariantViolationException.class, () -> WorkerName.of("  "));
    }
}
