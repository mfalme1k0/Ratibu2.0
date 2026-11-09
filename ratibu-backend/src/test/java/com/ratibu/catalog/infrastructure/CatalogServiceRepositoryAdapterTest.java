package com.ratibu.catalog.infrastructure;

import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.business.infrastructure.BusinessRepositoryAdapter;
import com.ratibu.catalog.domain.CatalogService;
import com.ratibu.catalog.domain.CatalogServiceRepository;
import com.ratibu.catalog.domain.ServiceName;
import com.ratibu.catalog.domain.ServiceStatus;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({CatalogServiceRepositoryAdapter.class, BusinessRepositoryAdapter.class})
@ActiveProfiles("test")
class CatalogServiceRepositoryAdapterTest {

    @Autowired
    private CatalogServiceRepository catalogServiceRepository;
    @Autowired
    private BusinessRepository businessRepository;

    private StableId businessId;

    @BeforeEach
    void seed() {
        Business business = Business.create(BusinessName.of("Beauty Empire"), CurrencyCode.of("KES"));
        businessId = business.getId();
        businessRepository.save(business);
    }

    @Test
    void save_and_findById_roundTrips() {
        CatalogService s = CatalogService.create(
                businessId, ServiceName.of("Haircut"), "Basic cut", Money.of(new BigDecimal("500.00")));
        catalogServiceRepository.save(s);

        CatalogService loaded = catalogServiceRepository.findById(s.getId()).orElseThrow();
        assertEquals(s.getId(), loaded.getId());
        assertEquals("Haircut", loaded.getName().asString());
        assertEquals(0, new BigDecimal("500.00").compareTo(loaded.getDefaultPrice().asBigDecimal()));
        assertEquals(ServiceStatus.ACTIVE, loaded.getStatus());
    }

    @Test
    void save_persistsDiscontinue() {
        CatalogService s = CatalogService.create(
                businessId, ServiceName.of("Color"), null, Money.of(new BigDecimal("1500")));
        catalogServiceRepository.save(s);
        s.discontinue();
        catalogServiceRepository.save(s);

        assertEquals(ServiceStatus.DISCONTINUED,
                catalogServiceRepository.findById(s.getId()).orElseThrow().getStatus());
    }

    @Test
    void findByBusinessId_listsServices() {
        catalogServiceRepository.save(CatalogService.create(
                businessId, ServiceName.of("A"), null, Money.of(100)));
        catalogServiceRepository.save(CatalogService.create(
                businessId, ServiceName.of("B"), null, Money.of(200)));
        assertEquals(2, catalogServiceRepository.findByBusinessId(businessId).size());
    }
}
