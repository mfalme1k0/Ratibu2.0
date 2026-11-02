package com.ratibu.business.application;

import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateBusinessServiceTest {

    private InMemoryBusinessRepository repository;
    private CreateBusinessService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBusinessRepository();
        service = new CreateBusinessService(repository);
    }

    @Test
    void execute_withoutClientId_createsNewBusiness() {
        CreateBusinessResult result = service.execute(
                CreateBusinessCommand.of("John's Barbershop", "KES"));

        assertNotNull(result.businessId());
        assertEquals("John's Barbershop", result.name());
        assertEquals("KES", result.currency());
        assertNotNull(result.createdAt());
        assertFalse(result.alreadyExisted());
        assertEquals(1, repository.size());
        assertTrue(repository.existsById(StableId.fromString(result.businessId())));
    }

    @Test
    void execute_withClientId_usesProvidedStableId() {
        String clientId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

        CreateBusinessResult result = service.execute(
                CreateBusinessCommand.of(clientId, "Mary's Salon", "USD"));

        assertEquals(clientId, result.businessId());
        assertEquals("Mary's Salon", result.name());
        assertEquals("USD", result.currency());
        assertFalse(result.alreadyExisted());
    }

    @Test
    void execute_withSameClientIdTwice_isIdempotent() {
        String clientId = "11111111-2222-3333-4444-555555555555";
        CreateBusinessCommand command = CreateBusinessCommand.of(clientId, "Fidel Auto", "KES");

        CreateBusinessResult first = service.execute(command);
        CreateBusinessResult second = service.execute(command);

        assertEquals(first.businessId(), second.businessId());
        assertEquals(first.createdAt(), second.createdAt());
        assertFalse(first.alreadyExisted());
        assertTrue(second.alreadyExisted());
        assertEquals(1, repository.size());
    }

    @Test
    void execute_rejectsBlankName() {
        assertThrows(InvariantViolationException.class,
                () -> service.execute(CreateBusinessCommand.of("  ", "KES")));
        assertEquals(0, repository.size());
    }

    @Test
    void execute_rejectsUnsupportedCurrency() {
        assertThrows(InvariantViolationException.class,
                () -> service.execute(CreateBusinessCommand.of("Shop", "JPY")));
        assertEquals(0, repository.size());
    }

    @Test
    void execute_normalizesCurrencyCase() {
        CreateBusinessResult result = service.execute(
                CreateBusinessCommand.of("Shop", "kes"));

        assertEquals("KES", result.currency());
    }
}
