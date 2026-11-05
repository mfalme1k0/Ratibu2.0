package com.ratibu.account.api;

import com.ratibu.account.application.CreateAccountResult;

import java.time.Instant;

public record AccountResponse(
        String accountId,
        String email,
        String displayName,
        Instant createdAt,
        boolean alreadyExisted
) {
    public static AccountResponse from(CreateAccountResult result) {
        return new AccountResponse(
                result.accountId(),
                result.email(),
                result.displayName(),
                result.createdAt(),
                result.alreadyExisted());
    }
}
