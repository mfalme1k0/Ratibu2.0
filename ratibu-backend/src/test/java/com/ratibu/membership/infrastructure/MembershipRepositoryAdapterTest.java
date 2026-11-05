package com.ratibu.membership.infrastructure;

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
@Import({MembershipRepositoryAdapter.class, AccountRepositoryAdapter.class, BusinessRepositoryAdapter.class})
@ActiveProfiles("test")
class MembershipRepositoryAdapterTest {

    @Autowired
    private MembershipRepository membershipRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BusinessRepository businessRepository;

    private StableId accountId;
    private StableId businessId;

    @BeforeEach
    void seed() {
        Account account = Account.create(EmailAddress.of("owner@x.com"), null);
        accountId = account.getId();
        accountRepository.save(account);

        Business business = Business.create(BusinessName.of("Biz"), CurrencyCode.of("KES"));
        businessId = business.getId();
        businessRepository.save(business);
    }

    @Test
    void save_and_findByPair_roundTrips() {
        Membership m = Membership.create(accountId, businessId, MembershipRole.OWNER);
        membershipRepository.save(m);

        Membership loaded = membershipRepository
                .findByAccountIdAndBusinessId(accountId, businessId)
                .orElseThrow();
        assertEquals(m.getId(), loaded.getId());
        assertEquals(MembershipRole.OWNER, loaded.getRole());
    }

    @Test
    void save_persistsRoleChange() {
        Membership m = Membership.create(accountId, businessId, MembershipRole.WORKER);
        membershipRepository.save(m);
        m.changeRole(MembershipRole.MANAGER);
        membershipRepository.save(m);

        assertEquals(MembershipRole.MANAGER,
                membershipRepository.findById(m.getId()).orElseThrow().getRole());
    }

    @Test
    void findByBusinessId_listsMembers() {
        membershipRepository.save(Membership.create(accountId, businessId, MembershipRole.OWNER));
        assertEquals(1, membershipRepository.findByBusinessId(businessId).size());
    }
}
