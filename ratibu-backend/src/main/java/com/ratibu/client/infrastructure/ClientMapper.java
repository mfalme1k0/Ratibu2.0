package com.ratibu.client.infrastructure;

import com.ratibu.client.domain.Client;
import com.ratibu.client.domain.ClientName;
import com.ratibu.client.domain.ClientStatus;
import com.ratibu.client.domain.PhoneNumber;
import com.ratibu.common.domain.StableId;

final class ClientMapper {

    private ClientMapper() {
    }

    static ClientJpaEntity toEntity(Client client) {
        return new ClientJpaEntity(
                client.getId().asUuid(),
                client.getBusinessId().asUuid(),
                client.getName().asString(),
                client.getPhone().map(PhoneNumber::asString).orElse(null),
                client.getEmail().orElse(null),
                client.getNotes().orElse(null),
                client.getStatus().name(),
                client.getCreatedAt());
    }

    static void updateEntity(ClientJpaEntity entity, Client client) {
        entity.setName(client.getName().asString());
        entity.setPhone(client.getPhone().map(PhoneNumber::asString).orElse(null));
        entity.setEmail(client.getEmail().orElse(null));
        entity.setNotes(client.getNotes().orElse(null));
        entity.setStatus(client.getStatus().name());
    }

    static Client toDomain(ClientJpaEntity entity) {
        return Client.reconstitute(
                StableId.fromUuid(entity.getId()),
                StableId.fromUuid(entity.getBusinessId()),
                ClientName.of(entity.getName()),
                entity.getPhone() != null ? PhoneNumber.of(entity.getPhone()) : null,
                entity.getEmail(),
                entity.getNotes(),
                ClientStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt());
    }
}
