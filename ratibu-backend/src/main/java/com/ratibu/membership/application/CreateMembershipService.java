package com.ratibu.membership.application;

import com.ratibu.account.domain.AccountRepository;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.exception.NotFoundException;
import com.ratibu.membership.domain.Membership;
import com.ratibu.membership.domain.MembershipRepository;
import com.ratibu.membership.domain.MembershipRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Grants an Account access to a Business with a role (ADR-002 §5–6).
 * Coordinates via repository ports only.
 */
@Service
public class CreateMembershipService {

    private final MembershipRepository membershipRepository;
    private final AccountRepository accountRepository;
    private final BusinessRepository businessRepository;

    public CreateMembershipService(
            MembershipRepository membershipRepository,
            AccountRepository accountRepository,
            BusinessRepository businessRepository) {
        this.membershipRepository = membershipRepository;
        this.accountRepository = accountRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public CreateMembershipResult execute(CreateMembershipCommand command) {
        StableId accountId = StableId.fromString(command.accountId());
        StableId businessId = StableId.fromString(command.businessId());
        MembershipRole role = parseRole(command.role());

        if (!accountRepository.existsById(accountId)) {
            throw new NotFoundException("Account not found: " + command.accountId());
        }
        if (!businessRepository.existsById(businessId)) {
            throw new NotFoundException("Business not found: " + command.businessId());
        }

        Optional<StableId> clientId = command.membershipId().map(StableId::fromString);

        if (clientId.isPresent()) {
            Optional<Membership> byId = membershipRepository.findById(clientId.get());
            if (byId.isPresent()) {
                return toResult(byId.get(), true);
            }
        }

        Optional<Membership> byPair = membershipRepository.findByAccountIdAndBusinessId(accountId, businessId);
        if (byPair.isPresent()) {
            return toResult(byPair.get(), true);
        }

        Membership created = clientId.isPresent()
                ? Membership.create(clientId.get(), accountId, businessId, role, Instant.now())
                : Membership.create(accountId, businessId, role);
        Membership saved = membershipRepository.save(created);
        saved.pullDomainEvents();
        return toResult(saved, false);
    }

    private static MembershipRole parseRole(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvariantViolationException("Membership role must not be blank");
        }
        try {
            return MembershipRole.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvariantViolationException(
                    "Membership role must be OWNER, MANAGER, or WORKER: " + raw);
        }
    }

    private static CreateMembershipResult toResult(Membership membership, boolean alreadyExisted) {
        return new CreateMembershipResult(
                membership.getId().toString(),
                membership.getAccountId().toString(),
                membership.getBusinessId().toString(),
                membership.getRole().name(),
                membership.getCreatedAt(),
                alreadyExisted);
    }
}
