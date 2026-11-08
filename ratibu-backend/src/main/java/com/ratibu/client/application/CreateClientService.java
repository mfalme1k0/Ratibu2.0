package com.ratibu.client.application;

import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.client.domain.Client;
import com.ratibu.client.domain.ClientName;
import com.ratibu.client.domain.ClientRepository;
import com.ratibu.client.domain.PhoneNumber;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Creates a Client under a Business (ADR-004 §3–§5, ADR-013 §7).
 * Phone is stored for soft matching later; create is never blocked by duplicate phone.
 */
@Service
public class CreateClientService {

    private final ClientRepository clientRepository;
    private final BusinessRepository businessRepository;

    public CreateClientService(ClientRepository clientRepository, BusinessRepository businessRepository) {
        this.clientRepository = clientRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public CreateClientResult execute(CreateClientCommand command) {
        StableId businessId = StableId.fromString(command.businessId());
        ClientName name = ClientName.of(command.name());

        if (!businessRepository.existsById(businessId)) {
            throw new NotFoundException("Business not found: " + command.businessId());
        }

        PhoneNumber phone = command.phone()
                .filter(p -> !p.isBlank())
                .map(PhoneNumber::of)
                .orElse(null);
        String email = command.email().filter(e -> !e.isBlank()).orElse(null);
        String notes = command.notes().filter(n -> !n.isBlank()).orElse(null);

        Optional<StableId> clientId = command.clientId().map(StableId::fromString);

        if (clientId.isPresent()) {
            Optional<Client> existing = clientRepository.findById(clientId.get());
            if (existing.isPresent()) {
                return toResult(existing.get(), true);
            }
        }

        Client created = clientId.isPresent()
                ? Client.create(clientId.get(), businessId, name, phone, email, notes, Instant.now())
                : Client.create(businessId, name, phone, email, notes);
        Client saved = clientRepository.save(created);
        saved.pullDomainEvents();
        return toResult(saved, false);
    }

    private static CreateClientResult toResult(Client client, boolean alreadyExisted) {
        return new CreateClientResult(
                client.getId().toString(),
                client.getBusinessId().toString(),
                client.getName().asString(),
                client.getPhone().map(PhoneNumber::asString).orElse(null),
                client.getEmail().orElse(null),
                client.getNotes().orElse(null),
                client.getStatus().name(),
                client.getCreatedAt(),
                alreadyExisted);
    }
}
