package com.ratibu.account.application;

import java.time.Instant;

public record CreateAccountResult(
        String accountId,
        String email,
        String displayName,
        Instant createdAt,
        boolean alreadyExisted
) {
}
