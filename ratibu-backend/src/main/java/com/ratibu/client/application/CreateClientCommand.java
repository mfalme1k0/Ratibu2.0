package com.ratibu.client.application;

import java.util.Optional;

public record CreateClientCommand(
        Optional<String> clientId,
        String businessId,
        String name,
        Optional<String> phone,
        Optional<String> email,
        Optional<String> notes
) {
    public CreateClientCommand {
        clientId = clientId == null ? Optional.empty() : clientId;
        phone = phone == null ? Optional.empty() : phone;
        email = email == null ? Optional.empty() : email;
        notes = notes == null ? Optional.empty() : notes;
    }

    public static CreateClientCommand of(String businessId, String name) {
        return new CreateClientCommand(
                Optional.empty(), businessId, name,
                Optional.empty(), Optional.empty(), Optional.empty());
    }
}
