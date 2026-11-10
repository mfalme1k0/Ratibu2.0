package com.ratibu.booking.api;

import com.ratibu.booking.application.CreateBookingCommand;
import com.ratibu.booking.application.CreateBookingResult;
import com.ratibu.booking.application.CreateBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Thin REST adapter for Booking (ADR-005, ADR-012 §18).
 */
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final CreateBookingService createBookingService;

    public BookingController(CreateBookingService createBookingService) {
        this.createBookingService = createBookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request) {
        CreateBookingCommand command = new CreateBookingCommand(
                Optional.ofNullable(request.bookingId()).filter(id -> !id.isBlank()),
                request.shopId(),
                request.clientId(),
                request.serviceId(),
                Optional.ofNullable(request.workerId()).filter(id -> !id.isBlank()),
                request.scheduledAt(),
                Optional.ofNullable(request.durationMinutes()),
                request.expectedPrice());

        CreateBookingResult result = createBookingService.execute(command);
        BookingResponse body = BookingResponse.from(result);
        HttpStatus status = result.alreadyExisted() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(body);
    }
}
