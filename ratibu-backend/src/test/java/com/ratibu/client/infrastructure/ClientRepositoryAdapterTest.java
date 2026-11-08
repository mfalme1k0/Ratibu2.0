package com.ratibu.client.infrastructure;

import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.business.infrastructure.BusinessRepositoryAdapter;
import com.ratibu.client.domain.Client;
import com.ratibu.client.domain.ClientName;
import com.ratibu.client.domain.ClientRepository;
import com.ratibu.client.domain.ClientStatus;
import com.ratibu.client.domain.PhoneNumber;
import com.ratibu.common.domain.StableId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ClientRepositoryAdapter.class, BusinessRepositoryAdapter.class})
@ActiveProfiles("test")
class ClientRepositoryAdapterTest {

    @Autowired
    private ClientRepository clientRepository;
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
        Client c = Client.create(businessId, ClientName.of("Jane"), null, null, null);
        clientRepository.save(c);

        Client loaded = clientRepository.findById(c.getId()).orElseThrow();
        assertEquals(c.getId(), loaded.getId());
        assertEquals("Jane", loaded.getName().asString());
        assertEquals(ClientStatus.ACTIVE, loaded.getStatus());
    }

    @Test
    void save_withPhone_findByBusinessAndPhone() {
        PhoneNumber phone = PhoneNumber.of("+254722222222");
        Client c = Client.create(businessId, ClientName.of("Jane"), phone, null, null);
        clientRepository.save(c);

        assertEquals(1, clientRepository.findByBusinessIdAndPhone(businessId, phone).size());
    }

    @Test
    void save_persistsDeactivate() {
        Client c = Client.create(businessId, ClientName.of("Jane"), null, null, null);
        clientRepository.save(c);
        c.deactivate();
        clientRepository.save(c);

        assertEquals(ClientStatus.INACTIVE,
                clientRepository.findById(c.getId()).orElseThrow().getStatus());
    }

    @Test
    void findByBusinessId_listsClients() {
        clientRepository.save(Client.create(businessId, ClientName.of("A"), null, null, null));
        clientRepository.save(Client.create(businessId, ClientName.of("B"), null, null, null));
        assertEquals(2, clientRepository.findByBusinessId(businessId).size());
    }

    @Test
    void duplicatePhone_bothPersisted() {
        PhoneNumber phone = PhoneNumber.of("+254733333333");
        clientRepository.save(Client.create(businessId, ClientName.of("One"), phone, null, null));
        clientRepository.save(Client.create(businessId, ClientName.of("Two"), phone, null, null));
        assertEquals(2, clientRepository.findByBusinessIdAndPhone(businessId, phone).size());
    }
}
