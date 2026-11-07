package com.ratibu.worker.application;

import com.ratibu.account.application.InMemoryAccountRepository;
import com.ratibu.account.domain.Account;
import com.ratibu.account.domain.EmailAddress;
import com.ratibu.business.application.InMemoryBusinessRepository;
import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.exception.NotFoundException;
import com.ratibu.membership.application.InMemoryMembershipRepository;
import com.ratibu.membership.domain.Membership;
import com.ratibu.membership.domain.MembershipRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateWorkerServiceTest {

    private InMemoryWorkerRepository workerRepository;
    private InMemoryBusinessRepository businessRepository;
    private InMemoryMembershipRepository membershipRepository;
    private CreateWorkerService service;
    private String businessId;
    private String membershipId;

    @BeforeEach
    void setUp() {
        workerRepository = new InMemoryWorkerRepository();
        businessRepository = new InMemoryBusinessRepository();
        membershipRepository = new InMemoryMembershipRepository();
        service = new CreateWorkerService(workerRepository, businessRepository, membershipRepository);

        Business business = Business.create(BusinessName.of("Beauty Empire"), CurrencyCode.of("KES"));
        businessRepository.save(business);
        businessId = business.getId().toString();

        Account account = Account.create(EmailAddress.of("mary@example.com"), null);
        // account not needed beyond membership creation
        Membership membership = Membership.create(
                account.getId(), business.getId(), MembershipRole.WORKER);
        membershipRepository.save(membership);
        membershipId = membership.getId().toString();
    }

    @Test
    void execute_createsWorkerWithoutMembership() {
        CreateWorkerResult result = service.execute(CreateWorkerCommand.of(businessId, "John Doe"));

        assertFalse(result.alreadyExisted());
        assertEquals("John Doe", result.name());
        assertEquals(businessId, result.businessId());
        assertNull(result.membershipId());
        assertEquals("ACTIVE", result.status());
    }

    @Test
    void execute_createsWorkerWithMembership() {
        CreateWorkerResult result = service.execute(
                CreateWorkerCommand.of(businessId, "Mary", membershipId));

        assertFalse(result.alreadyExisted());
        assertEquals(membershipId, result.membershipId());
    }

    @Test
    void execute_sameWorkerId_isIdempotent() {
        String workerId = "22222222-2222-2222-2222-222222222222";
        CreateWorkerCommand cmd = new CreateWorkerCommand(
                java.util.Optional.of(workerId), businessId, "Mary", java.util.Optional.empty());

        CreateWorkerResult first = service.execute(cmd);
        CreateWorkerResult second = service.execute(cmd);

        assertEquals(first.workerId(), second.workerId());
        assertTrue(second.alreadyExisted());
    }

    @Test
    void execute_sameMembership_isIdempotent() {
        CreateWorkerResult first = service.execute(
                CreateWorkerCommand.of(businessId, "Mary", membershipId));
        CreateWorkerResult second = service.execute(
                CreateWorkerCommand.of(businessId, "Mary Again", membershipId));

        assertEquals(first.workerId(), second.workerId());
        assertTrue(second.alreadyExisted());
        assertEquals("Mary", second.name()); // original retained
    }

    @Test
    void execute_rejectsUnknownBusiness() {
        assertThrows(NotFoundException.class,
                () -> service.execute(CreateWorkerCommand.of(
                        "22222222-2222-2222-2222-222222222222", "X")));
    }

    @Test
    void execute_rejectsUnknownMembership() {
        assertThrows(NotFoundException.class,
                () -> service.execute(CreateWorkerCommand.of(
                        businessId, "X", "33333333-3333-3333-3333-333333333333")));
    }

    @Test
    void execute_rejectsBlankName() {
        assertThrows(InvariantViolationException.class,
                () -> service.execute(CreateWorkerCommand.of(businessId, "  ")));
    }
}
