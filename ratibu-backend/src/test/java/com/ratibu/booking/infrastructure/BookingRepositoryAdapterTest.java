package com.ratibu.booking.infrastructure;

import com.ratibu.booking.domain.Booking;
import com.ratibu.booking.domain.BookingRepository;
import com.ratibu.booking.domain.BookingStatus;
import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.business.infrastructure.BusinessRepositoryAdapter;
import com.ratibu.catalog.domain.CatalogService;
import com.ratibu.catalog.domain.CatalogServiceRepository;
import com.ratibu.catalog.domain.ServiceName;
import com.ratibu.catalog.infrastructure.CatalogServiceRepositoryAdapter;
import com.ratibu.client.domain.Client;
import com.ratibu.client.domain.ClientName;
import com.ratibu.client.domain.ClientRepository;
import com.ratibu.client.infrastructure.ClientRepositoryAdapter;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;
import com.ratibu.shop.domain.Shop;
import com.ratibu.shop.domain.ShopName;
import com.ratibu.shop.domain.ShopRepository;
import com.ratibu.shop.infrastructure.ShopRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        BookingRepositoryAdapter.class,
        ShopRepositoryAdapter.class,
        ClientRepositoryAdapter.class,
        CatalogServiceRepositoryAdapter.class,
        BusinessRepositoryAdapter.class
})
@ActiveProfiles("test")
class BookingRepositoryAdapterTest {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private ShopRepository shopRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private CatalogServiceRepository catalogServiceRepository;
    @Autowired private BusinessRepository businessRepository;

    private StableId shopId;
    private StableId clientId;
    private StableId serviceId;

    @BeforeEach
    void seed() {
        Business business = Business.create(BusinessName.of("Biz"), CurrencyCode.of("KES"));
        businessRepository.save(business);
        Shop shop = Shop.create(business.getId(), ShopName.of("Westlands"));
        shopRepository.save(shop);
        shopId = shop.getId();
        Client client = Client.create(business.getId(), ClientName.of("Jane"), null, null, null);
        clientRepository.save(client);
        clientId = client.getId();
        CatalogService service = CatalogService.create(
                business.getId(), ServiceName.of("Haircut"), null, Money.of(new BigDecimal("500")));
        catalogServiceRepository.save(service);
        serviceId = service.getId();
    }

    @Test
    void save_and_findById_roundTrips() {
        Booking b = Booking.create(
                shopId, clientId, serviceId, null,
                Instant.parse("2026-12-01T10:00:00Z"), 45, Money.of(new BigDecimal("500.00")));
        bookingRepository.save(b);

        Booking loaded = bookingRepository.findById(b.getId()).orElseThrow();
        assertEquals(BookingStatus.SCHEDULED, loaded.getStatus());
        assertEquals(0, new BigDecimal("500.00").compareTo(loaded.getExpectedPrice().asBigDecimal()));
    }

    @Test
    void save_persistsLifecycle() {
        Booking b = Booking.create(
                shopId, clientId, serviceId, null,
                Instant.parse("2026-12-01T10:00:00Z"), null, Money.of(new BigDecimal("500")));
        bookingRepository.save(b);
        b.start();
        b.complete(Money.of(new BigDecimal("480")));
        bookingRepository.save(b);

        Booking loaded = bookingRepository.findById(b.getId()).orElseThrow();
        assertEquals(BookingStatus.COMPLETED, loaded.getStatus());
        assertEquals(0, new BigDecimal("480").compareTo(loaded.getFinalPrice().orElseThrow().asBigDecimal()));
    }
}
