package com.ratibu.shop.infrastructure;

import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.business.infrastructure.BusinessRepositoryAdapter;
import com.ratibu.common.domain.StableId;
import com.ratibu.shop.domain.Shop;
import com.ratibu.shop.domain.ShopName;
import com.ratibu.shop.domain.ShopRepository;
import com.ratibu.shop.domain.ShopStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ShopRepositoryAdapter.class, BusinessRepositoryAdapter.class})
@ActiveProfiles("test")
class ShopRepositoryAdapterTest {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private BusinessRepository businessRepository;

    private StableId businessId;

    @BeforeEach
    void seedBusiness() {
        Business business = Business.create(BusinessName.of("Test Biz"), CurrencyCode.of("KES"));
        businessId = business.getId();
        businessRepository.save(business);
    }

    @Test
    void save_and_findById_roundTrips() {
        Shop shop = Shop.create(businessId, ShopName.of("Westlands"));
        Shop saved = shopRepository.save(shop);

        assertEquals(shop.getId(), saved.getId());
        assertEquals(businessId, saved.getBusinessId());
        assertEquals("Westlands", saved.getName().asString());
        assertEquals(ShopStatus.ACTIVE, saved.getStatus());

        Shop loaded = shopRepository.findById(shop.getId()).orElseThrow();
        assertEquals(shop.getId(), loaded.getId());
        assertTrue(loaded.pullDomainEvents().isEmpty());
    }

    @Test
    void save_persistsRenameAndClose() {
        Shop shop = Shop.create(businessId, ShopName.of("Old"));
        shopRepository.save(shop);

        shop.rename(ShopName.of("Westlands Premium"));
        shop.close();
        shopRepository.save(shop);

        Shop loaded = shopRepository.findById(shop.getId()).orElseThrow();
        assertEquals("Westlands Premium", loaded.getName().asString());
        assertEquals(ShopStatus.CLOSED, loaded.getStatus());
    }

    @Test
    void findByBusinessId_returnsShopsForBusiness() {
        Shop a = Shop.create(businessId, ShopName.of("A"));
        Shop b = Shop.create(businessId, ShopName.of("B"));
        shopRepository.save(a);
        shopRepository.save(b);

        List<Shop> shops = shopRepository.findByBusinessId(businessId);
        assertEquals(2, shops.size());
    }

    @Test
    void existsById_reflectsPersistence() {
        Shop shop = Shop.create(businessId, ShopName.of("Exists"));
        assertFalse(shopRepository.existsById(shop.getId()));
        shopRepository.save(shop);
        assertTrue(shopRepository.existsById(shop.getId()));
    }
}
