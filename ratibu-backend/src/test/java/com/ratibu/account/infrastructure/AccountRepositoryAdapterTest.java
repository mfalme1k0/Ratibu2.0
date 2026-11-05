package com.ratibu.account.infrastructure;

import com.ratibu.account.domain.Account;
import com.ratibu.account.domain.AccountRepository;
import com.ratibu.account.domain.DisplayName;
import com.ratibu.account.domain.EmailAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AccountRepositoryAdapter.class)
@ActiveProfiles("test")
class AccountRepositoryAdapterTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void save_and_findByEmail_roundTrips() {
        Account account = Account.create(
                EmailAddress.of("owner@beauty.empire"),
                DisplayName.of("Owner"));
        accountRepository.save(account);

        Account loaded = accountRepository.findByEmail(EmailAddress.of("owner@beauty.empire")).orElseThrow();
        assertEquals(account.getId(), loaded.getId());
        assertEquals("Owner", loaded.getDisplayName().orElseThrow().asString());
        assertTrue(accountRepository.existsByEmail(EmailAddress.of("OWNER@beauty.empire")));
    }

    @Test
    void save_updatesDisplayName() {
        Account account = Account.create(EmailAddress.of("a@b.co"), DisplayName.of("A"));
        accountRepository.save(account);
        account.rename(DisplayName.of("B"));
        accountRepository.save(account);

        assertEquals("B", accountRepository.findById(account.getId()).orElseThrow()
                .getDisplayName().orElseThrow().asString());
    }
}
