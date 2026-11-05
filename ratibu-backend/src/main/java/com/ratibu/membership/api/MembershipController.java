package com.ratibu.membership.api;

import com.ratibu.membership.application.CreateMembershipCommand;
import com.ratibu.membership.application.CreateMembershipResult;
import com.ratibu.membership.application.CreateMembershipService;
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
 * Thin REST adapter for Business Membership (ADR-002 §5, ADR-012 §18).
 */
@RestController
@RequestMapping("/api/v1/businesses/{businessId}/memberships")
public class MembershipController {

    private final CreateMembershipService createMembershipService;

    public MembershipController(CreateMembershipService createMembershipService) {
        this.createMembershipService = createMembershipService;
    }

    @PostMapping
    public ResponseEntity<MembershipResponse> create(
            @PathVariable String businessId,
            @Valid @RequestBody CreateMembershipRequest request) {

        CreateMembershipCommand command = new CreateMembershipCommand(
                Optional.ofNullable(request.membershipId()).filter(id -> !id.isBlank()),
                request.accountId(),
                businessId,
                request.role());

        CreateMembershipResult result = createMembershipService.execute(command);
        MembershipResponse body = MembershipResponse.from(result);

        HttpStatus status = result.alreadyExisted() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(body);
    }
}
