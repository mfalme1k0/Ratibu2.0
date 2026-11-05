package com.ratibu.membership.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MembershipTest {

    private final StableId accountId = StableId.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final StableId businessId = StableId.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void create_raisesMembershipCreated() {
        Membership m = Membership.create(accountId, businessId, MembershipRole.OWNER);

        assertEquals(MembershipRole.OWNER, m.getRole());
        assertEquals(accountId, m.getAccountId());
        assertEquals(businessId, m.getBusinessId());

        List<DomainEvent> events = m.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(MembershipCreated.class, events.getFirst());
    }

    @Test
    void reconstitute_doesNotRaiseEvents() {
        Membership m = Membership.reconstitute(
                StableId.generate(), accountId, businessId, MembershipRole.WORKER, Instant.now());
        assertTrue(m.pullDomainEvents().isEmpty());
    }

    @Test
    void changeRole_updatesRole() {
        Membership m = Membership.create(accountId, businessId, MembershipRole.WORKER);
        m.pullDomainEvents();
        m.changeRole(MembershipRole.MANAGER);
        assertEquals(MembershipRole.MANAGER, m.getRole());
    }

    @Test
    void create_rejectsNullRole() {
        assertThrows(InvariantViolationException.class,
                () -> Membership.create(accountId, businessId, null));
    }
}
