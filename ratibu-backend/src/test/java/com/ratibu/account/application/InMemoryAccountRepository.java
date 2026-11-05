package com.ratibu.account.application;

import com.ratibu.account.domain.Account;
import com.ratibu.account.domain.AccountRepository;
import com.ratibu.account.domain.EmailAddress;
import com.ratibu.common.domain.StableId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryAccountRepository implements AccountRepository {

    private final Map<StableId, Account> byId = new ConcurrentHashMap<>();
    private final Map<String, StableId> emailIndex = new ConcurrentHashMap<>();

    @Override
    public Account save(Account account) {
        Account snapshot = Account.reconstitute(
                account.getId(),
                account.getEmail(),
                account.getDisplayName().orElse(null),
                account.getCreatedAt());
        byId.put(snapshot.getId(), snapshot);
        emailIndex.put(snapshot.getEmail().asString(), snapshot.getId());
        return snapshot;
    }

    @Override
    public Optional<Account> findById(StableId id) {
        return Optional.ofNullable(byId.get(id)).map(this::copy);
    }

    @Override
    public Optional<Account> findByEmail(EmailAddress email) {
        StableId id = emailIndex.get(email.asString());
        return id == null ? Optional.empty() : findById(id);
    }

    @Override
    public boolean existsById(StableId id) {
        return byId.containsKey(id);
    }

    @Override
    public boolean existsByEmail(EmailAddress email) {
        return emailIndex.containsKey(email.asString());
    }

    private Account copy(Account a) {
        return Account.reconstitute(a.getId(), a.getEmail(), a.getDisplayName().orElse(null), a.getCreatedAt());
    }
}
