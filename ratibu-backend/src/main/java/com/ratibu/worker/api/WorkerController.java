package com.ratibu.worker.api;

import com.ratibu.worker.application.CreateWorkerCommand;
import com.ratibu.worker.application.CreateWorkerResult;
import com.ratibu.worker.application.CreateWorkerService;
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
 * Thin REST adapter for Worker Profile (ADR-004 §8, ADR-012 §18).
 */
@RestController
@RequestMapping("/api/v1/businesses/{businessId}/workers")
public class WorkerController {

    private final CreateWorkerService createWorkerService;

    public WorkerController(CreateWorkerService createWorkerService) {
        this.createWorkerService = createWorkerService;
    }

    @PostMapping
    public ResponseEntity<WorkerResponse> create(
            @PathVariable String businessId,
            @Valid @RequestBody CreateWorkerRequest request) {

        CreateWorkerCommand command = new CreateWorkerCommand(
                Optional.ofNullable(request.workerId()).filter(id -> !id.isBlank()),
                businessId,
                request.name(),
                Optional.ofNullable(request.membershipId()).filter(id -> !id.isBlank()));

        CreateWorkerResult result = createWorkerService.execute(command);
        WorkerResponse body = WorkerResponse.from(result);

        HttpStatus status = result.alreadyExisted() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(body);
    }
}
