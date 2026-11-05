package com.ratibu.account.application;

import java.util.Optional;

public record CreateAccountCommand(
        Optional<String> accountId,
        String email,
        Optional<String> displayName
) {
    public CreateAccountCommand {
        accountId = accountId == null ? Optional.empty() : accountId;
        displayName = displayName == null ? Optional.empty() : displayName;
    }

    public static CreateAccountCommand of(String email, String displayName) {
        return new CreateAccountCommand(Optional.empty(), email, Optional.ofNullable(displayName));
    }

    public static CreateAccountCommand of(String accountId, String email, String displayName) {
        return new CreateAccountCommand(Optional.ofNullable(accountId), email, Optional.ofNullable(displayName));
    }
}
