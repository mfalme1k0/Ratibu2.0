package com.ratibu.business.infrastructure;

import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.common.domain.StableId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BusinessRepositoryAdapter.class)
@ActiveProfiles("test")
class BusinessRepositoryAdapterTest {

    @Autowired
    private BusinessRepository businessRepository;

    @Test
    void save_and_findById_roundTripsDomainAggregate() {
        Business created = Business.create(
                BusinessName.of("John's Barbershop"),
                CurrencyCode.of("KES"));
        StableId id = created.getId();
        Instant createdAt = created.getCreatedAt();

        Business saved = businessRepository.save(created);

        assertEquals(id, saved.getId());
        assertEquals("John's Barbershop", saved.getName().asString());
        assertEquals("KES", saved.getCurrency().asString());
        assertEquals(createdAt, saved.getCreatedAt());

        Optional<Business> loaded = businessRepository.findById(id);
        assertTrue(loaded.isPresent());
        assertEquals(id, loaded.get().getId());
        assertEquals("John's Barbershop", loaded.get().getName().asString());
        assertEquals("KES", loaded.get().getCurrency().asString());
        // reconstituted aggregates do not carry create events
        assertTrue(loaded.get().pullDomainEvents().isEmpty());
    }

    @Test
    void save_updatesName_withoutChangingCurrencyOrId() {
        Business created = Business.create(
                BusinessName.of("Old Name"),
                CurrencyCode.of("USD"));
        businessRepository.save(created);

        created.rename(BusinessName.of("New Name"));
        Business updated = businessRepository.save(created);

        assertEquals(created.getId(), updated.getId());
        assertEquals("New Name", updated.getName().asString());
        assertEquals("USD", updated.getCurrency().asString());

        Business reloaded = businessRepository.findById(created.getId()).orElseThrow();
        assertEquals("New Name", reloaded.getName().asString());
        assertEquals("USD", reloaded.getCurrency().asString());
    }

    @Test
    void existsById_reflectsPersistence() {
        Business created = Business.create(
                BusinessName.of("Exists Check"),
                CurrencyCode.of("EUR"));

        assertFalse(businessRepository.existsById(created.getId()));
        businessRepository.save(created);
        assertTrue(businessRepository.existsById(created.getId()));
        assertFalse(businessRepository.existsById(StableId.generate()));
    }

    @Test
    void findById_missing_returnsEmpty() {
        assertTrue(businessRepository.findById(StableId.generate()).isEmpty());
    }
}
