package com.ratibu.catalog.api;

import com.ratibu.catalog.application.CreateCatalogServiceCommand;
import com.ratibu.catalog.application.CreateCatalogServiceResult;
import com.ratibu.catalog.application.CreateCatalogServiceService;
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
 * Thin REST adapter for catalog services (ADR-004 §14, ADR-012 §18).
 */
@RestController
@RequestMapping("/api/v1/businesses/{businessId}/services")
public class CatalogServiceController {

    private final CreateCatalogServiceService createCatalogServiceService;

    public CatalogServiceController(CreateCatalogServiceService createCatalogServiceService) {
        this.createCatalogServiceService = createCatalogServiceService;
    }

    @PostMapping
    public ResponseEntity<CatalogServiceResponse> create(
            @PathVariable String businessId,
            @Valid @RequestBody CreateCatalogServiceRequest request) {

        CreateCatalogServiceCommand command = new CreateCatalogServiceCommand(
                Optional.ofNullable(request.serviceId()).filter(id -> !id.isBlank()),
                businessId,
                request.name(),
                Optional.ofNullable(request.description()),
                request.defaultPrice());

        CreateCatalogServiceResult result = createCatalogServiceService.execute(command);
        CatalogServiceResponse body = CatalogServiceResponse.from(result);

        HttpStatus status = result.alreadyExisted() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(body);
    }
}
