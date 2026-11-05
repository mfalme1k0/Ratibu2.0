package com.ratibu.account.domain;

import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void create_raisesAccountCreated() {
        Account account = Account.create(
                EmailAddress.of("jane@example.com"),
                DisplayName.of("Jane"));

        assertNotNull(account.getId());
        assertEquals("jane@example.com", account.getEmail().asString());
        assertEquals("Jane", account.getDisplayName().orElseThrow().asString());

        List<DomainEvent> events = account.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(AccountCreated.class, events.getFirst());
    }

    @Test
    void create_allowsNullDisplayName() {
        Account account = Account.create(EmailAddress.of("a@b.co"), null);
        assertTrue(account.getDisplayName().isEmpty());
    }

    @Test
    void reconstitute_doesNotRaiseEvents() {
        Account account = Account.reconstitute(
                StableId.generate(),
                EmailAddress.of("a@b.co"),
                null,
                Instant.now());
        assertTrue(account.pullDomainEvents().isEmpty());
    }

    @Test
    void rename_updatesDisplayName() {
        Account account = Account.create(EmailAddress.of("a@b.co"), DisplayName.of("Old"));
        account.pullDomainEvents();
        account.rename(DisplayName.of("New"));
        assertEquals("New", account.getDisplayName().orElseThrow().asString());
    }
}
