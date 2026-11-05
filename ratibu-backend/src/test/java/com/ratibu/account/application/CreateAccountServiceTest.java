package com.ratibu.account.application;

import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateAccountServiceTest {

    private InMemoryAccountRepository repository;
    private CreateAccountService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAccountRepository();
        service = new CreateAccountService(repository);
    }

    @Test
    void execute_createsAccount() {
        CreateAccountResult result = service.execute(
                CreateAccountCommand.of("jane@example.com", "Jane"));

        assertFalse(result.alreadyExisted());
        assertEquals("jane@example.com", result.email());
        assertEquals("Jane", result.displayName());
        assertNotNull(result.accountId());
    }

    @Test
    void execute_sameEmail_isIdempotent() {
        CreateAccountResult first = service.execute(
                CreateAccountCommand.of("jane@example.com", "Jane"));
        CreateAccountResult second = service.execute(
                CreateAccountCommand.of("Jane@Example.com", "Jane Doe"));

        assertEquals(first.accountId(), second.accountId());
        assertTrue(second.alreadyExisted());
    }

    @Test
    void execute_withClientId_isIdempotent() {
        String id = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        CreateAccountResult first = service.execute(
                CreateAccountCommand.of(id, "a@b.co", "A"));
        CreateAccountResult second = service.execute(
                CreateAccountCommand.of(id, "a@b.co", "A"));

        assertEquals(id, first.accountId());
        assertTrue(second.alreadyExisted());
    }

    @Test
    void execute_rejectsEmailTakenByDifferentId() {
        service.execute(CreateAccountCommand.of("taken@x.com", "One"));
        assertThrows(InvariantViolationException.class,
                () -> service.execute(CreateAccountCommand.of(
                        "11111111-1111-1111-1111-111111111111",
                        "taken@x.com",
                        "Two")));
    }
}
