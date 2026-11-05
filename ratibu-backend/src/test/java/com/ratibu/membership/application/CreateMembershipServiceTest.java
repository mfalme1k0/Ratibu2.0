package com.ratibu.membership.application;

import com.ratibu.account.application.InMemoryAccountRepository;
import com.ratibu.account.domain.Account;
import com.ratibu.account.domain.EmailAddress;
import com.ratibu.business.application.InMemoryBusinessRepository;
import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateMembershipServiceTest {

    private InMemoryMembershipRepository membershipRepository;
    private InMemoryAccountRepository accountRepository;
    private InMemoryBusinessRepository businessRepository;
    private CreateMembershipService service;
    private String accountId;
    private String businessId;

    @BeforeEach
    void setUp() {
        membershipRepository = new InMemoryMembershipRepository();
        accountRepository = new InMemoryAccountRepository();
        businessRepository = new InMemoryBusinessRepository();
        service = new CreateMembershipService(membershipRepository, accountRepository, businessRepository);

        Account account = Account.create(EmailAddress.of("a@b.co"), null);
        accountRepository.save(account);
        accountId = account.getId().toString();

        Business business = Business.create(BusinessName.of("Biz"), CurrencyCode.of("KES"));
        businessRepository.save(business);
        businessId = business.getId().toString();
    }

    @Test
    void execute_createsOwnerMembership() {
        CreateMembershipResult result = service.execute(
                CreateMembershipCommand.of(accountId, businessId, "OWNER"));

        assertFalse(result.alreadyExisted());
        assertEquals("OWNER", result.role());
        assertEquals(accountId, result.accountId());
        assertEquals(businessId, result.businessId());
    }

    @Test
    void execute_samePair_isIdempotent() {
        CreateMembershipResult first = service.execute(
                CreateMembershipCommand.of(accountId, businessId, "OWNER"));
        CreateMembershipResult second = service.execute(
                CreateMembershipCommand.of(accountId, businessId, "WORKER"));

        assertEquals(first.membershipId(), second.membershipId());
        assertTrue(second.alreadyExisted());
        // existing membership is returned as-is (role not overwritten on idempotent replay)
        assertEquals("OWNER", second.role());
    }

    @Test
    void execute_rejectsUnknownAccount() {
        assertThrows(NotFoundException.class,
                () -> service.execute(CreateMembershipCommand.of(
                        "11111111-1111-1111-1111-111111111111", businessId, "OWNER")));
    }

    @Test
    void execute_rejectsUnknownBusiness() {
        assertThrows(NotFoundException.class,
                () -> service.execute(CreateMembershipCommand.of(
                        accountId, "22222222-2222-2222-2222-222222222222", "OWNER")));
    }

    @Test
    void execute_rejectsInvalidRole() {
        assertThrows(InvariantViolationException.class,
                () -> service.execute(CreateMembershipCommand.of(accountId, businessId, "SUPERUSER")));
    }
}
