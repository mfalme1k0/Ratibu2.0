package com.ratibu.account.domain;

import com.ratibu.common.domain.StableId;

import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(StableId id);

    Optional<Account> findByEmail(EmailAddress email);

    boolean existsById(StableId id);

    boolean existsByEmail(EmailAddress email);
}
