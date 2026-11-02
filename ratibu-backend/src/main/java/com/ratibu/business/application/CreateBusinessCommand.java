package com.ratibu.business.application;

import java.util.Optional;

/**
 * Input for the create-business use case.
 *
 * <p>{@code businessId} is optional: when present (client-generated
 * {@link com.ratibu.common.domain.StableId}), creation is idempotent for
 * offline sync (ADR-010 §13–14). When absent, the server generates a new id.
 */
public record CreateBusinessCommand(
        Optional<String> businessId,
        String name,
        String currency
) {
    public CreateBusinessCommand {
        businessId = businessId == null ? Optional.empty() : businessId;
    }

    public static CreateBusinessCommand of(String name, String currency) {
        return new CreateBusinessCommand(Optional.empty(), name, currency);
    }

    public static CreateBusinessCommand of(String businessId, String name, String currency) {
        return new CreateBusinessCommand(Optional.ofNullable(businessId), name, currency);
    }
}
