package com.ratibu.client.api;

import com.ratibu.client.application.CreateClientCommand;
import com.ratibu.client.application.CreateClientResult;
import com.ratibu.client.application.CreateClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Thin REST adapter for Client (ADR-004 §3, ADR-012 §18).
 */
@RestController
@RequestMapping("/api/v1/businesses/{businessId}/clients")
public class ClientController {

    private final CreateClientService createClientService;

    public ClientController(CreateClientService createClientService) {
        this.createClientService = createClientService;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(
            @PathVariable String businessId,
            @Valid @RequestBody CreateClientRequest request) {

        CreateClientCommand command = new CreateClientCommand(
                Optional.ofNullable(request.clientId()).filter(id -> !id.isBlank()),
                businessId,
                request.name(),
                Optional.ofNullable(request.phone()),
                Optional.ofNullable(request.email()),
                Optional.ofNullable(request.notes()));

        CreateClientResult result = createClientService.execute(command);
        ClientResponse body = ClientResponse.from(result);

        HttpStatus status = result.alreadyExisted() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(body);
    }
}
