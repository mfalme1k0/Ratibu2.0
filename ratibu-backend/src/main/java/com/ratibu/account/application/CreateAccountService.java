package com.ratibu.account.application;

import com.ratibu.account.domain.Account;
import com.ratibu.account.domain.AccountRepository;
import com.ratibu.account.domain.DisplayName;
import com.ratibu.account.domain.EmailAddress;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Registers a User Account (ADR-002 §4). Email uniqueness is enforced here
 * and at the database. Idempotent when client StableId matches an existing row.
 */
@Service
public class CreateAccountService {

    private final AccountRepository accountRepository;

    public CreateAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public CreateAccountResult execute(CreateAccountCommand command) {
        EmailAddress email = EmailAddress.of(command.email());
        DisplayName displayName = command.displayName()
                .filter(s -> !s.isBlank())
                .map(DisplayName::of)
                .orElse(null);

        Optional<StableId> clientId = command.accountId().map(StableId::fromString);

        if (clientId.isPresent()) {
            StableId id = clientId.get();
            Optional<Account> byId = accountRepository.findById(id);
            if (byId.isPresent()) {
                return toResult(byId.get(), true);
            }
        }

        Optional<Account> byEmail = accountRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            // Same email, different client id → conflict for MVP
            if (clientId.isPresent() && !byEmail.get().getId().equals(clientId.get())) {
                throw new InvariantViolationException(
                        "Email is already registered: " + email.asString());
            }
            return toResult(byEmail.get(), true);
        }

        Account created = clientId.isPresent()
                ? Account.create(clientId.get(), email, displayName, Instant.now())
                : Account.create(email, displayName);
        Account saved = accountRepository.save(created);
        saved.pullDomainEvents();
        return toResult(saved, false);
    }

    private static CreateAccountResult toResult(Account account, boolean alreadyExisted) {
        return new CreateAccountResult(
                account.getId().toString(),
                account.getEmail().asString(),
                account.getDisplayName().map(DisplayName::asString).orElse(null),
                account.getCreatedAt(),
                alreadyExisted);
    }
}
