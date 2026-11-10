package com.ratibu.booking.application;

import com.ratibu.booking.domain.Booking;
import com.ratibu.booking.domain.BookingRepository;
import com.ratibu.catalog.domain.CatalogService;
import com.ratibu.catalog.domain.CatalogServiceRepository;
import com.ratibu.client.domain.Client;
import com.ratibu.client.domain.ClientRepository;
import com.ratibu.common.domain.Money;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import com.ratibu.common.exception.NotFoundException;
import com.ratibu.shop.domain.Shop;
import com.ratibu.shop.domain.ShopRepository;
import com.ratibu.worker.domain.Worker;
import com.ratibu.worker.domain.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Creates a Booking in SCHEDULED status (ADR-005 §4–§11).
 * Validates shop/client/service share a business; worker optional.
 * Worker–service capability hard-block (ADR-013 §4) deferred until capabilities land.
 */
@Service
public class CreateBookingService {

    private final BookingRepository bookingRepository;
    private final ShopRepository shopRepository;
    private final ClientRepository clientRepository;
    private final CatalogServiceRepository catalogServiceRepository;
    private final WorkerRepository workerRepository;

    public CreateBookingService(
            BookingRepository bookingRepository,
            ShopRepository shopRepository,
            ClientRepository clientRepository,
            CatalogServiceRepository catalogServiceRepository,
            WorkerRepository workerRepository) {
        this.bookingRepository = bookingRepository;
        this.shopRepository = shopRepository;
        this.clientRepository = clientRepository;
        this.catalogServiceRepository = catalogServiceRepository;
        this.workerRepository = workerRepository;
    }

    @Transactional
    public CreateBookingResult execute(CreateBookingCommand command) {
        if (command.scheduledAt() == null) {
            throw new InvariantViolationException("scheduledAt is required");
        }
        if (command.expectedPrice() == null) {
            throw new InvariantViolationException("expectedPrice is required");
        }

        StableId shopId = StableId.fromString(command.shopId());
        StableId clientId = StableId.fromString(command.clientId());
        StableId serviceId = StableId.fromString(command.serviceId());

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found: " + command.shopId()));
        if (!shop.isActive()) {
            throw new InvariantViolationException("Cannot book at a closed shop");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found: " + command.clientId()));
        if (!client.getBusinessId().equals(shop.getBusinessId())) {
            throw new InvariantViolationException("Client does not belong to the shop's business");
        }
        if (!client.isActive()) {
            throw new InvariantViolationException("Cannot book an inactive client");
        }

        CatalogService catalogService = catalogServiceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found: " + command.serviceId()));
        if (!catalogService.getBusinessId().equals(shop.getBusinessId())) {
            throw new InvariantViolationException("Service does not belong to the shop's business");
        }
        if (!catalogService.isActive()) {
            throw new InvariantViolationException("Cannot book a discontinued service");
        }

        StableId workerId = null;
        if (command.workerId().isPresent() && !command.workerId().get().isBlank()) {
            workerId = StableId.fromString(command.workerId().get());
            Worker worker = workerRepository.findById(workerId)
                    .orElseThrow(() -> new NotFoundException("Worker not found: " + command.workerId().get()));
            if (!worker.getBusinessId().equals(shop.getBusinessId())) {
                throw new InvariantViolationException("Worker does not belong to the shop's business");
            }
            if (!worker.isActive()) {
                throw new InvariantViolationException("Cannot assign an inactive worker");
            }
        }

        Money expectedPrice = Money.of(command.expectedPrice());
        Integer duration = command.durationMinutes().orElse(null);

        Optional<StableId> clientBookingId = command.bookingId().map(StableId::fromString);
        if (clientBookingId.isPresent()) {
            Optional<Booking> existing = bookingRepository.findById(clientBookingId.get());
            if (existing.isPresent()) {
                return toResult(existing.get(), true);
            }
        }

        Booking created = clientBookingId.isPresent()
                ? Booking.create(
                        clientBookingId.get(), shopId, clientId, serviceId, workerId,
                        command.scheduledAt(), duration, expectedPrice, Instant.now())
                : Booking.create(
                        shopId, clientId, serviceId, workerId,
                        command.scheduledAt(), duration, expectedPrice);
        Booking saved = bookingRepository.save(created);
        saved.pullDomainEvents();
        return toResult(saved, false);
    }

    private static CreateBookingResult toResult(Booking booking, boolean alreadyExisted) {
        return new CreateBookingResult(
                booking.getId().toString(),
                booking.getShopId().toString(),
                booking.getClientId().toString(),
                booking.getServiceId().toString(),
                booking.getWorkerId().map(StableId::toString).orElse(null),
                booking.getScheduledAt(),
                booking.getDurationMinutes().orElse(null),
                booking.getExpectedPrice().asBigDecimal(),
                booking.getFinalPrice().map(Money::asBigDecimal).orElse(null),
                booking.getStatus().name(),
                booking.getCreatedAt(),
                alreadyExisted);
    }
}
