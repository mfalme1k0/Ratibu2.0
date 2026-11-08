package com.ratibu.client.application;

import com.ratibu.business.application.InMemoryBusinessRepository;
import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CreateClientServiceTest {

    private InMemoryClientRepository clientRepository;
    private InMemoryBusinessRepository businessRepository;
    private CreateClientService service;
    private String businessId;

    @BeforeEach
    void setUp() {
        clientRepository = new InMemoryClientRepository();
        businessRepository = new InMemoryBusinessRepository();
        service = new CreateClientService(clientRepository, businessRepository);

        Business business = Business.create(BusinessName.of("Beauty Empire"), CurrencyCode.of("KES"));
        businessRepository.save(business);
        businessId = business.getId().toString();
    }

    @Test
    void execute_createsClient() {
        CreateClientResult result = service.execute(CreateClientCommand.of(businessId, "Jane"));

        assertFalse(result.alreadyExisted());
        assertEquals("Jane", result.name());
        assertEquals(businessId, result.businessId());
        assertEquals("ACTIVE", result.status());
        assertNull(result.phone());
    }

    @Test
    void execute_withPhoneAndEmail() {
        CreateClientCommand cmd = new CreateClientCommand(
                Optional.empty(), businessId, "Jane",
                Optional.of("+254711111111"), Optional.of("j@x.com"), Optional.of("note"));
        CreateClientResult result = service.execute(cmd);

        assertEquals("+254711111111", result.phone());
        assertEquals("j@x.com", result.email());
        assertEquals("note", result.notes());
    }

    @Test
    void execute_sameClientId_isIdempotent() {
        String clientId = "33333333-3333-3333-3333-333333333333";
        CreateClientCommand cmd = new CreateClientCommand(
                Optional.of(clientId), businessId, "Jane",
                Optional.empty(), Optional.empty(), Optional.empty());

        CreateClientResult first = service.execute(cmd);
        CreateClientResult second = service.execute(cmd);

        assertEquals(first.clientId(), second.clientId());
        assertTrue(second.alreadyExisted());
    }

    @Test
    void execute_allowsDuplicatePhone_noAutoMerge() {
        CreateClientCommand a = new CreateClientCommand(
                Optional.empty(), businessId, "Jane",
                Optional.of("+254700000001"), Optional.empty(), Optional.empty());
        CreateClientCommand b = new CreateClientCommand(
                Optional.empty(), businessId, "Jane Other",
                Optional.of("+254700000001"), Optional.empty(), Optional.empty());

        CreateClientResult first = service.execute(a);
        CreateClientResult second = service.execute(b);

        assertNotEquals(first.clientId(), second.clientId());
        assertFalse(second.alreadyExisted());
    }

    @Test
    void execute_rejectsUnknownBusiness() {
        assertThrows(NotFoundException.class,
                () -> service.execute(CreateClientCommand.of(
                        "22222222-2222-2222-2222-222222222222", "X")));
    }

    @Test
    void execute_rejectsBlankName() {
        assertThrows(InvariantViolationException.class,
                () -> service.execute(CreateClientCommand.of(businessId, "  ")));
    }
}
