package com.ratibu.account.infrastructure;

import com.ratibu.account.domain.Account;
import com.ratibu.account.domain.DisplayName;
import com.ratibu.account.domain.EmailAddress;
import com.ratibu.common.domain.StableId;

final class AccountMapper {

    private AccountMapper() {
    }

    static AccountJpaEntity toEntity(Account account) {
        return new AccountJpaEntity(
                account.getId().asUuid(),
                account.getEmail().asString(),
                account.getDisplayName().map(DisplayName::asString).orElse(null),
                account.getCreatedAt());
    }

    static void updateEntity(AccountJpaEntity entity, Account account) {
        entity.setDisplayName(account.getDisplayName().map(DisplayName::asString).orElse(null));
    }

    static Account toDomain(AccountJpaEntity entity) {
        DisplayName displayName = entity.getDisplayName() == null || entity.getDisplayName().isBlank()
                ? null
                : DisplayName.of(entity.getDisplayName());
        return Account.reconstitute(
                StableId.fromUuid(entity.getId()),
                EmailAddress.of(entity.getEmail()),
                displayName,
                entity.getCreatedAt());
    }
}
