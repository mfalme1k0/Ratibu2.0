package com.ratibu.shop.api;

import com.ratibu.shop.application.CreateShopCommand;
import com.ratibu.shop.application.CreateShopResult;
import com.ratibu.shop.application.CreateShopService;
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
 * Thin REST adapter for the Shop aggregate (ADR-012 §18).
 */
@RestController
@RequestMapping("/api/v1/businesses/{businessId}/shops")
public class ShopController {

    private final CreateShopService createShopService;

    public ShopController(CreateShopService createShopService) {
        this.createShopService = createShopService;
    }

    @PostMapping
    public ResponseEntity<ShopResponse> create(
            @PathVariable String businessId,
            @Valid @RequestBody CreateShopRequest request) {

        CreateShopCommand command = new CreateShopCommand(
                Optional.ofNullable(request.shopId()).filter(id -> !id.isBlank()),
                businessId,
                request.name());

        CreateShopResult result = createShopService.execute(command);
        ShopResponse body = ShopResponse.from(result);

        HttpStatus status = result.alreadyExisted() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(body);
    }
}
