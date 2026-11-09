package com.ratibu.catalog.application;

import com.ratibu.business.application.InMemoryBusinessRepository;
import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CreateCatalogServiceServiceTest {

    private InMemoryCatalogServiceRepository catalogRepository;
    private InMemoryBusinessRepository businessRepository;
    private CreateCatalogServiceService service;
    private String businessId;

    @BeforeEach
    void setUp() {
        catalogRepository = new InMemoryCatalogServiceRepository();
        businessRepository = new InMemoryBusinessRepository();
        service = new CreateCatalogServiceService(catalogRepository, businessRepository);

        Business business = Business.create(BusinessName.of("Beauty Empire"), CurrencyCode.of("KES"));
        businessRepository.save(business);
        businessId = business.getId().toString();
    }

    @Test
    void execute_createsService() {
        CreateCatalogServiceResult result = service.execute(
                CreateCatalogServiceCommand.of(businessId, "Haircut", new BigDecimal("500.00")));

        assertFalse(result.alreadyExisted());
        assertEquals("Haircut", result.name());
        assertEquals(0, new BigDecimal("500.00").compareTo(result.defaultPrice()));
        assertEquals("ACTIVE", result.status());
    }

    @Test
    void execute_sameServiceId_isIdempotent() {
        String serviceId = "44444444-4444-4444-4444-444444444444";
        CreateCatalogServiceCommand cmd = new CreateCatalogServiceCommand(
                Optional.of(serviceId), businessId, "Haircut", Optional.empty(), new BigDecimal("500"));

        CreateCatalogServiceResult first = service.execute(cmd);
        CreateCatalogServiceResult second = service.execute(cmd);

        assertEquals(first.serviceId(), second.serviceId());
        assertTrue(second.alreadyExisted());
    }

    @Test
    void execute_rejectsUnknownBusiness() {
        assertThrows(NotFoundException.class,
                () -> service.execute(CreateCatalogServiceCommand.of(
                        "22222222-2222-2222-2222-222222222222", "X", BigDecimal.TEN)));
    }

    @Test
    void execute_rejectsNullPrice() {
        assertThrows(InvariantViolationException.class,
                () -> service.execute(new CreateCatalogServiceCommand(
                        Optional.empty(), businessId, "Haircut", Optional.empty(), null)));
    }

    @Test
    void execute_rejectsBlankName() {
        assertThrows(InvariantViolationException.class,
                () -> service.execute(CreateCatalogServiceCommand.of(businessId, "  ", BigDecimal.ONE)));
    }
}
