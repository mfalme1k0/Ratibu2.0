package com.ratibu.booking.application;

import com.ratibu.business.application.InMemoryBusinessRepository;
import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.catalog.application.InMemoryCatalogServiceRepository;
import com.ratibu.catalog.domain.CatalogService;
import com.ratibu.catalog.domain.ServiceName;
import com.ratibu.client.application.InMemoryClientRepository;
import com.ratibu.client.domain.Client;
import com.ratibu.client.domain.ClientName;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.exception.NotFoundException;
import com.ratibu.shop.domain.Shop;
import com.ratibu.shop.domain.ShopName;
import com.ratibu.shop.domain.ShopRepository;
import com.ratibu.shop.domain.ShopStatus;
import com.ratibu.worker.application.InMemoryWorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class CreateBookingServiceTest {

    private InMemoryBookingRepository bookingRepository;
    private TestShopRepository shopRepository;
    private InMemoryClientRepository clientRepository;
    private InMemoryCatalogServiceRepository catalogRepository;
    private InMemoryWorkerRepository workerRepository;
    private CreateBookingService service;

    private String shopId;
    private String clientId;
    private String serviceId;
    private final Instant scheduledAt = Instant.parse("2026-12-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        bookingRepository = new InMemoryBookingRepository();
        shopRepository = new TestShopRepository();
        clientRepository = new InMemoryClientRepository();
        catalogRepository = new InMemoryCatalogServiceRepository();
        workerRepository = new InMemoryWorkerRepository();
        service = new CreateBookingService(
                bookingRepository, shopRepository, clientRepository, catalogRepository, workerRepository);

        Business business = Business.create(BusinessName.of("Biz"), CurrencyCode.of("KES"));
        Shop shop = Shop.create(business.getId(), ShopName.of("Westlands"));
        shopRepository.save(shop);
        shopId = shop.getId().toString();

        Client client = Client.create(business.getId(), ClientName.of("Jane"), null, null, null);
        clientRepository.save(client);
        clientId = client.getId().toString();

        CatalogService catalogService = CatalogService.create(
                business.getId(), ServiceName.of("Haircut"), null, Money.of(new BigDecimal("500")));
        catalogRepository.save(catalogService);
        serviceId = catalogService.getId().toString();
    }

    @Test
    void execute_createsScheduledBooking() {
        CreateBookingResult result = service.execute(new CreateBookingCommand(
                Optional.empty(), shopId, clientId, serviceId, Optional.empty(),
                scheduledAt, Optional.of(30), new BigDecimal("500.00")));

        assertFalse(result.alreadyExisted());
        assertEquals("SCHEDULED", result.status());
        assertEquals(0, new BigDecimal("500.00").compareTo(result.expectedPrice()));
        assertNull(result.finalPrice());
    }

    @Test
    void execute_sameBookingId_isIdempotent() {
        String bookingId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        CreateBookingCommand cmd = new CreateBookingCommand(
                Optional.of(bookingId), shopId, clientId, serviceId, Optional.empty(),
                scheduledAt, Optional.empty(), new BigDecimal("500"));

        assertEquals(service.execute(cmd).bookingId(), service.execute(cmd).bookingId());
        assertTrue(service.execute(cmd).alreadyExisted());
    }

    @Test
    void execute_rejectsUnknownShop() {
        assertThrows(NotFoundException.class, () -> service.execute(new CreateBookingCommand(
                Optional.empty(), "99999999-9999-9999-9999-999999999999", clientId, serviceId,
                Optional.empty(), scheduledAt, Optional.empty(), BigDecimal.TEN)));
    }

    @Test
    void execute_rejectsMismatchedBusinessClient() {
        Business other = Business.create(BusinessName.of("Other"), CurrencyCode.of("KES"));
        Client foreign = Client.create(other.getId(), ClientName.of("X"), null, null, null);
        clientRepository.save(foreign);

        assertThrows(InvariantViolationException.class, () -> service.execute(new CreateBookingCommand(
                Optional.empty(), shopId, foreign.getId().toString(), serviceId,
                Optional.empty(), scheduledAt, Optional.empty(), BigDecimal.TEN)));
    }

    /** Minimal public shop repo for cross-package tests (InMemoryShopRepository is package-private). */
    static final class TestShopRepository implements ShopRepository {
        private final Map<StableId, Shop> store = new ConcurrentHashMap<>();

        @Override
        public Shop save(Shop shop) {
            store.put(shop.getId(), shop);
            return shop;
        }

        @Override
        public Optional<Shop> findById(StableId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public boolean existsById(StableId id) {
            return store.containsKey(id);
        }

        @Override
        public List<Shop> findByBusinessId(StableId businessId) {
            return store.values().stream().filter(s -> s.getBusinessId().equals(businessId)).toList();
        }
    }
}
