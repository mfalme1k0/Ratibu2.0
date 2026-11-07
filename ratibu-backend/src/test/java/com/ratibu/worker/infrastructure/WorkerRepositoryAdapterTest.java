package com.ratibu.worker.infrastructure;

import com.ratibu.account.domain.Account;
import com.ratibu.account.domain.AccountRepository;
import com.ratibu.account.domain.EmailAddress;
import com.ratibu.account.infrastructure.AccountRepositoryAdapter;
import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.business.infrastructure.BusinessRepositoryAdapter;
import com.ratibu.common.domain.StableId;
import com.ratibu.membership.domain.Membership;
import com.ratibu.membership.domain.MembershipRepository;
import com.ratibu.membership.domain.MembershipRole;
import com.ratibu.membership.infrastructure.MembershipRepositoryAdapter;
import com.ratibu.worker.domain.Worker;
import com.ratibu.worker.domain.WorkerName;
import com.ratibu.worker.domain.WorkerRepository;
import com.ratibu.worker.domain.WorkerStatus;
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
@Import({
        WorkerRepositoryAdapter.class,
        MembershipRepositoryAdapter.class,
        AccountRepositoryAdapter.class,
        BusinessRepositoryAdapter.class
})
@ActiveProfiles("test")
class WorkerRepositoryAdapterTest {

    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private MembershipRepository membershipRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BusinessRepository businessRepository;

    private StableId businessId;
    private StableId membershipId;

    @BeforeEach
    void seed() {
        Business business = Business.create(BusinessName.of("Beauty Empire"), CurrencyCode.of("KES"));
        businessId = business.getId();
        businessRepository.save(business);

        Account account = Account.create(EmailAddress.of("worker@x.com"), null);
        accountRepository.save(account);

        Membership membership = Membership.create(account.getId(), businessId, MembershipRole.WORKER);
        membershipId = membership.getId();
        membershipRepository.save(membership);
    }

    @Test
    void save_and_findById_roundTrips() {
        Worker w = Worker.create(businessId, WorkerName.of("Mary"), null);
        workerRepository.save(w);

        Worker loaded = workerRepository.findById(w.getId()).orElseThrow();
        assertEquals(w.getId(), loaded.getId());
        assertEquals("Mary", loaded.getName().asString());
        assertEquals(WorkerStatus.ACTIVE, loaded.getStatus());
        assertTrue(loaded.getMembershipId().isEmpty());
    }

    @Test
    void save_withMembership_roundTrips() {
        Worker w = Worker.create(businessId, WorkerName.of("Mary"), membershipId);
        workerRepository.save(w);

        Worker byMembership = workerRepository.findByMembershipId(membershipId).orElseThrow();
        assertEquals(w.getId(), byMembership.getId());
        assertEquals(membershipId, byMembership.getMembershipId().orElseThrow());
    }

    @Test
    void save_persistsDeactivate() {
        Worker w = Worker.create(businessId, WorkerName.of("John"), null);
        workerRepository.save(w);
        w.deactivate();
        workerRepository.save(w);

        assertEquals(WorkerStatus.INACTIVE,
                workerRepository.findById(w.getId()).orElseThrow().getStatus());
    }

    @Test
    void findByBusinessId_listsWorkers() {
        workerRepository.save(Worker.create(businessId, WorkerName.of("A"), null));
        workerRepository.save(Worker.create(businessId, WorkerName.of("B"), null));
        assertEquals(2, workerRepository.findByBusinessId(businessId).size());
    }
}
