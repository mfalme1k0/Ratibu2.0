package com.ratibu.shop.application;

import com.ratibu.business.application.InMemoryBusinessRepository;
import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateShopServiceTest {

    private InMemoryBusinessRepository businessRepository;
    private InMemoryShopRepository shopRepository;
    private CreateShopService service;
    private String businessId;

    @BeforeEach
    void setUp() {
        businessRepository = new InMemoryBusinessRepository();
        shopRepository = new InMemoryShopRepository();
        service = new CreateShopService(shopRepository, businessRepository);

        Business business = Business.create(BusinessName.of("Biz"), CurrencyCode.of("KES"));
        businessRepository.save(business);
        businessId = business.getId().toString();
    }

    @Test
    void execute_createsShopUnderBusiness() {
        CreateShopResult result = service.execute(CreateShopCommand.of(businessId, "Westlands"));

        assertNotNull(result.shopId());
        assertEquals(businessId, result.businessId());
        assertEquals("Westlands", result.name());
        assertEquals("ACTIVE", result.status());
        assertFalse(result.alreadyExisted());
    }

    @Test
    void execute_withClientId_isIdempotent() {
        String shopId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        CreateShopCommand cmd = CreateShopCommand.of(shopId, businessId, "Kilimani");

        CreateShopResult first = service.execute(cmd);
        CreateShopResult second = service.execute(cmd);

        assertEquals(shopId, first.shopId());
        assertFalse(first.alreadyExisted());
        assertTrue(second.alreadyExisted());
        assertEquals(first.createdAt(), second.createdAt());
    }

    @Test
    void execute_rejectsUnknownBusiness() {
        assertThrows(NotFoundException.class,
                () -> service.execute(CreateShopCommand.of(
                        StableId.generate().toString(), "Ghost Shop")));
    }

    @Test
    void execute_rejectsBlankName() {
        assertThrows(InvariantViolationException.class,
                () -> service.execute(CreateShopCommand.of(businessId, "  ")));
    }
}
