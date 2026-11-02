package com.ratibu.business.api;

import com.ratibu.business.application.CreateBusinessCommand;
import com.ratibu.business.application.CreateBusinessResult;
import com.ratibu.business.application.CreateBusinessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Thin REST adapter for the Business aggregate (ADR-012 §18).
 * Validation + mapping only; all rules live in domain / application.
 */
@RestController
@RequestMapping("/api/v1/businesses")
public class BusinessController {

    private final CreateBusinessService createBusinessService;

    public BusinessController(CreateBusinessService createBusinessService) {
        this.createBusinessService = createBusinessService;
    }

    @PostMapping
    public ResponseEntity<BusinessResponse> create(@Valid @RequestBody CreateBusinessRequest request) {
        CreateBusinessCommand command = new CreateBusinessCommand(
                Optional.ofNullable(request.businessId()).filter(id -> !id.isBlank()),
                request.name(),
                request.currency());

        CreateBusinessResult result = createBusinessService.execute(command);
        BusinessResponse body = BusinessResponse.from(result);

        HttpStatus status = result.alreadyExisted() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(body);
    }
}
