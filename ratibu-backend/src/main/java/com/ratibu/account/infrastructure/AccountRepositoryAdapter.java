package com.ratibu.account.infrastructure;

import com.ratibu.account.domain.Account;
import com.ratibu.account.domain.AccountRepository;
import com.ratibu.account.domain.EmailAddress;
import com.ratibu.common.domain.StableId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class AccountRepositoryAdapter implements AccountRepository {

    private final SpringDataAccountRepository springData;

    public AccountRepositoryAdapter(SpringDataAccountRepository springData) {
        this.springData = springData;
    }

    @Override
    public Account save(Account account) {
        Optional<AccountJpaEntity> existing = springData.findById(account.getId().asUuid());
        AccountJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            AccountMapper.updateEntity(entity, account);
        } else {
            entity = AccountMapper.toEntity(account);
        }
        return AccountMapper.toDomain(springData.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findById(StableId id) {
        return springData.findById(id.asUuid()).map(AccountMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(EmailAddress email) {
        return springData.findByEmail(email.asString()).map(AccountMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(StableId id) {
        return springData.existsById(id.asUuid());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(EmailAddress email) {
        return springData.existsByEmail(email.asString());
    }
}
