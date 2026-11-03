package com.ratibu.business;

import com.ratibu.business.domain.Business;
import com.ratibu.business.domain.BusinessCreated;
import com.ratibu.business.domain.BusinessName;
import com.ratibu.business.domain.BusinessRepository;
import com.ratibu.business.domain.CurrencyCode;
import com.ratibu.business.infrastructure.BusinessRepositoryAdapter;
import com.ratibu.common.domain.DomainEvent;
import com.ratibu.common.domain.StableId;
import com.ratibu.common.exception.InvariantViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end tests for the Business slice.
 *
 * <p>Boots the full Spring context (no slice test), runs the real Flyway
 * migration, and drives the aggregate through the domain port
 * ({@link BusinessRepository}) into the database. Every assertion about
 * persisted state is made by reading the row back with raw JDBC, so the tests
 * do not trust the JPA mapping to check itself.
 *
 * <p>Tests are deliberately NOT {@code @Transactional}: the adapter commits
 * per call, which is what happens in production, and a rollback-per-test
 * would hide flush/commit-time failures.
 *
 * <p>NOTE: the slice has no application or API layer yet (see
 * {@code business/api} and {@code business/application}, both package-info
 * only). When create-business / rename-business use cases and controllers
 * land, add a MockMvc/TestRestTemplate layer on top of these.
 */
@SpringBootTest
@ActiveProfiles("test")
class BusinessE2ETest {

    @Autowired
    private BusinessRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM business");
    }

    // ------------------------------------------------------------------ helpers

    private Business newBusiness(String name, String currency) {
        return Business.create(BusinessName.of(name), CurrencyCode.of(currency));
    }

    private long rowCount() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM business", Long.class);
        return count == null ? 0 : count;
    }

    private Map<String, Object> rowFor(StableId id) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, name, currency, created_at FROM business WHERE id = ?",
                id.asUuid());
        assertThat(rows).as("rows for id %s", id).hasSize(1);
        return rows.getFirst();
    }

    private Instant createdAtOf(StableId id) {
        Object value = rowFor(id).get("created_at");
        return ((OffsetDateTime) value).toInstant();
    }

    private void rawInsert(UUID id, String name, String currency, Instant createdAt) {
        jdbc.update(
                "INSERT INTO business (id, name, currency, created_at) VALUES (?, ?, ?, ?)",
                id, name, currency, OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC));
    }

    /** Postgres/H2 timestamp columns keep microseconds; avoid nanosecond flakiness. */
    private static Instant fixedInstant() {
        return Instant.parse("2026-01-15T10:00:00.123456Z");
    }

    // ------------------------------------------------------------------ wiring

    @Test
    void contextWiresDomainPortToJpaAdapter() {
        assertThat(repository).isInstanceOf(BusinessRepositoryAdapter.class);
    }

    @Test
    void flywayAppliedV1AndCreatedBusinessTableAndIndex() {
        Integer applied = jdbc.queryForObject(
                "SELECT COUNT(*) FROM \"flyway_schema_history\" "
                        + "WHERE \"version\" = '1' AND \"success\" = TRUE",
                Integer.class);
        assertThat(applied).isEqualTo(1);

        Integer index = jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES "
                        + "WHERE LOWER(TABLE_NAME) = 'business' "
                        + "AND LOWER(INDEX_NAME) = 'idx_business_created_at'",
                Integer.class);
        assertThat(index).isEqualTo(1);
    }

    // ------------------------------------------------------------------ lifecycle

    @Nested
    class Lifecycle {

        @Test
        void createSaveReload_persistsExactlyWhatTheAggregateHolds() {
            Business business = Business.create(
                    StableId.generate(),
                    BusinessName.of("John's Barbershop"),
                    CurrencyCode.of("KES"),
                    fixedInstant());

            repository.save(business);

            Map<String, Object> row = rowFor(business.getId());
            assertThat(row.get("id")).isEqualTo(business.getId().asUuid());
            assertThat(row.get("name")).isEqualTo("John's Barbershop");
            assertThat(row.get("currency")).isEqualTo("KES");
            assertThat(createdAtOf(business.getId())).isEqualTo(fixedInstant());

            Business loaded = repository.findById(business.getId()).orElseThrow();
            assertThat(loaded.getId()).isEqualTo(business.getId());
            assertThat(loaded.getName()).isEqualTo(business.getName());
            assertThat(loaded.getCurrency()).isEqualTo(business.getCurrency());
            assertThat(loaded.getCreatedAt()).isEqualTo(fixedInstant());
        }

        @Test
        void rename_isPersisted_andLeavesIdCurrencyAndCreatedAtUntouched() {
            Business business = Business.create(
                    StableId.generate(), BusinessName.of("Old Name"),
                    CurrencyCode.of("USD"), fixedInstant());
            repository.save(business);

            Business loaded = repository.findById(business.getId()).orElseThrow();
            loaded.rename(BusinessName.of("New Name"));
            repository.save(loaded);

            Map<String, Object> row = rowFor(business.getId());
            assertThat(row.get("name")).isEqualTo("New Name");
            assertThat(row.get("currency")).isEqualTo("USD");
            assertThat(createdAtOf(business.getId())).isEqualTo(fixedInstant());
            assertThat(rowCount()).isEqualTo(1);
        }

        @Test
        void multipleRenames_lastWriteWins() {
            Business business = newBusiness("v1", "KES");
            repository.save(business);

            for (String name : List.of("v2", "v3", "v4")) {
                Business loaded = repository.findById(business.getId()).orElseThrow();
                loaded.rename(BusinessName.of(name));
                repository.save(loaded);
            }

            assertThat(rowFor(business.getId()).get("name")).isEqualTo("v4");
            assertThat(rowCount()).isEqualTo(1);
        }

        @Test
        void unknownId_isAbsent() {
            StableId unknown = StableId.generate();
            assertThat(repository.existsById(unknown)).isFalse();
            assertThat(repository.findById(unknown)).isEmpty();
        }

        @Test
        void existsById_flipsToTrueAfterSave() {
            Business business = newBusiness("Exists Check", "EUR");
            assertThat(repository.existsById(business.getId())).isFalse();
            repository.save(business);
            assertThat(repository.existsById(business.getId())).isTrue();
        }
    }

    // ------------------------------------------------------------------ domain events

    @Nested
    class DomainEvents {

        @Test
        void businessCreated_matchesWhatWasPersisted() {
            Business business = newBusiness("Event Shop", "TZS");
            repository.save(business);

            List<DomainEvent> events = business.pullDomainEvents();
            assertThat(events).hasSize(1);
            BusinessCreated event = (BusinessCreated) events.getFirst();

            Map<String, Object> row = rowFor(business.getId());
            assertThat(event.businessId().asUuid()).isEqualTo(row.get("id"));
            assertThat(event.name().asString()).isEqualTo(row.get("name"));
            assertThat(event.currency().asString()).isEqualTo(row.get("currency"));
        }

        @Test
        void savingDoesNotDrainOrDuplicateEvents() {
            Business business = newBusiness("Event Shop", "KES");
            repository.save(business);
            repository.save(business);

            // Still exactly one BusinessCreated; the repository must not consume events.
            assertThat(business.pullDomainEvents()).hasSize(1);
        }

        @Test
        void reloadedAggregate_carriesNoEvents() {
            Business business = newBusiness("Reload", "KES");
            repository.save(business);

            Business loaded = repository.findById(business.getId()).orElseThrow();
            assertThat(loaded.pullDomainEvents()).isEmpty();
        }
    }

    // ------------------------------------------------------------------ offline sync (ADR-010)

    @Nested
    class OfflineSync {

        @Test
        void clientSuppliedStableIdAndTimestamp_arePreserved() {
            StableId clientId = StableId.fromString("11111111-1111-1111-1111-111111111111");
            Business business = Business.create(
                    clientId, BusinessName.of("Created Offline"),
                    CurrencyCode.of("KES"), fixedInstant());

            repository.save(business);

            assertThat(rowFor(clientId).get("id"))
                    .isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            assertThat(createdAtOf(clientId)).isEqualTo(fixedInstant());
        }

        @Test
        void replayingTheSameCreate_isIdempotent_noDuplicateRow() {
            StableId id = StableId.generate();
            // Same payload delivered twice (retry after a dropped ack).
            Business first = Business.create(id, BusinessName.of("Dup"), CurrencyCode.of("KES"), fixedInstant());
            Business replay = Business.create(id, BusinessName.of("Dup"), CurrencyCode.of("KES"), fixedInstant());

            repository.save(first);
            repository.save(replay);

            assertThat(rowCount()).isEqualTo(1);
            assertThat(rowFor(id).get("name")).isEqualTo("Dup");
        }

        @Test
        void replayWithDifferentCurrency_cannotChangePersistedCurrency() {
            StableId id = StableId.generate();
            repository.save(Business.create(id, BusinessName.of("Fixed Currency"),
                    CurrencyCode.of("KES"), fixedInstant()));

            // Currency is immutable after create (ADR-011 §49). A conflicting replay must not rewrite it.
            repository.save(Business.create(id, BusinessName.of("Fixed Currency"),
                    CurrencyCode.of("USD"), fixedInstant()));

            assertThat(rowFor(id).get("currency")).isEqualTo("KES");
            assertThat(rowCount()).isEqualTo(1);
        }

        @Test
        void replayWithDifferentCreatedAt_doesNotRewriteOriginalTimestamp() {
            StableId id = StableId.generate();
            repository.save(Business.create(id, BusinessName.of("Stamp"),
                    CurrencyCode.of("KES"), fixedInstant()));

            repository.save(Business.create(id, BusinessName.of("Stamp"),
                    CurrencyCode.of("KES"), fixedInstant().plus(1, ChronoUnit.DAYS)));

            assertThat(createdAtOf(id)).isEqualTo(fixedInstant());
        }
    }

    // ------------------------------------------------------------------ invariants never reach the DB

    @Nested
    class InvariantsAtTheBoundary {

        @Test
        void nameIsTrimmedBeforePersisting() {
            Business business = newBusiness("   Padded Name   ", "KES");
            repository.save(business);
            assertThat(rowFor(business.getId()).get("name")).isEqualTo("Padded Name");
        }

        @Test
        void nameAtMaxLength_roundTrips() {
            String max = "a".repeat(BusinessName.MAX_LENGTH);
            Business business = newBusiness(max, "KES");
            repository.save(business);

            assertThat(repository.findById(business.getId()).orElseThrow().getName().asString())
                    .isEqualTo(max);
        }

        @Test
        void nameAtMaxLength_withMultibyteCharacters_roundTrips() {
            String max = "é".repeat(BusinessName.MAX_LENGTH);
            Business business = newBusiness(max, "KES");
            repository.save(business);

            assertThat(rowFor(business.getId()).get("name")).isEqualTo(max);
        }

        @Test
        void unicodeAndPunctuationInName_roundTrip() {
            String name = "Mama Fua's Salon — Kibera / 美容院 ✂";
            Business business = newBusiness(name, "KES");
            repository.save(business);

            assertThat(repository.findById(business.getId()).orElseThrow().getName().asString())
                    .isEqualTo(name);
        }

        @Test
        void nameOverMaxLength_isRejected_andNothingIsPersisted() {
            assertThatThrownBy(() -> newBusiness("a".repeat(BusinessName.MAX_LENGTH + 1), "KES"))
                    .isInstanceOf(InvariantViolationException.class);
            assertThat(rowCount()).isZero();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        void blankName_isRejected_andNothingIsPersisted(String blank) {
            assertThatThrownBy(() -> newBusiness(blank, "KES"))
                    .isInstanceOf(InvariantViolationException.class);
            assertThat(rowCount()).isZero();
        }

        @Test
        void renameToInvalidName_leavesPersistedNameUnchanged() {
            Business business = newBusiness("Stable", "KES");
            repository.save(business);

            Business loaded = repository.findById(business.getId()).orElseThrow();
            assertThatThrownBy(() -> loaded.rename(null))
                    .isInstanceOf(InvariantViolationException.class);
            assertThatThrownBy(() -> loaded.rename(BusinessName.of(" ")))
                    .isInstanceOf(InvariantViolationException.class);

            assertThat(rowFor(business.getId()).get("name")).isEqualTo("Stable");
        }

        @ParameterizedTest
        @ValueSource(strings = {"KES", "USD", "EUR", "GBP", "UGX", "TZS", "RWF"})
        void everySupportedCurrency_roundTrips(String code) {
            Business business = newBusiness("Currency " + code, code);
            repository.save(business);

            assertThat(rowFor(business.getId()).get("currency")).isEqualTo(code);
            assertThat(repository.findById(business.getId()).orElseThrow().getCurrency().asString())
                    .isEqualTo(code);
        }

        @Test
        void lowercaseCurrency_isNormalisedToUpperCaseInTheDatabase() {
            Business business = newBusiness("Lower", " kes ");
            repository.save(business);
            assertThat(rowFor(business.getId()).get("currency")).isEqualTo("KES");
        }

        @ParameterizedTest
        @ValueSource(strings = {"JPY", "XXX", "KE", "KESS", "12A"})
        void unsupportedOrMalformedCurrency_isRejected_andNothingIsPersisted(String code) {
            assertThatThrownBy(() -> newBusiness("Bad Currency", code))
                    .isInstanceOf(InvariantViolationException.class);
            assertThat(rowCount()).isZero();
        }

        @Test
        void nullId_isRejected() {
            assertThatThrownBy(() -> Business.create(
                    null, BusinessName.of("No Id"), CurrencyCode.of("KES"), Instant.now()))
                    .isInstanceOf(InvariantViolationException.class);
        }
    }

    // ------------------------------------------------------------------ multiple aggregates

    @Nested
    class MultipleBusinesses {

        @Test
        void businessesAreIsolatedFromEachOther() {
            Business a = newBusiness("Shop A", "KES");
            Business b = newBusiness("Shop B", "USD");
            repository.save(a);
            repository.save(b);

            Business loadedA = repository.findById(a.getId()).orElseThrow();
            loadedA.rename(BusinessName.of("Shop A Renamed"));
            repository.save(loadedA);

            assertThat(rowFor(a.getId()).get("name")).isEqualTo("Shop A Renamed");
            assertThat(rowFor(b.getId()).get("name")).isEqualTo("Shop B");
            assertThat(rowFor(b.getId()).get("currency")).isEqualTo("USD");
            assertThat(rowCount()).isEqualTo(2);
        }

        @Test
        void sameNameIsAllowed_nameIsNotAUniqueKey() {
            Business a = newBusiness("Barbershop", "KES");
            Business b = newBusiness("Barbershop", "KES");
            repository.save(a);
            repository.save(b);

            assertThat(a.getId()).isNotEqualTo(b.getId());
            assertThat(rowCount()).isEqualTo(2);
        }
    }

    // ------------------------------------------------------------------ schema contract (V1__business.sql)

    @Nested
    class SchemaContract {

        @Test
        void nullName_isRejectedByTheDatabase() {
            assertThatThrownBy(() -> rawInsert(UUID.randomUUID(), null, "KES", fixedInstant()))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void nullCurrency_isRejectedByTheDatabase() {
            assertThatThrownBy(() -> rawInsert(UUID.randomUUID(), "x", null, fixedInstant()))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void nameLongerThan120_isRejectedByTheDatabase() {
            assertThatThrownBy(() -> rawInsert(UUID.randomUUID(), "a".repeat(121), "KES", fixedInstant()))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void currencyLongerThan3_isRejectedByTheDatabase() {
            assertThatThrownBy(() -> rawInsert(UUID.randomUUID(), "x", "KESX", fixedInstant()))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void duplicateId_isRejectedByTheDatabase() {
            UUID id = UUID.randomUUID();
            rawInsert(id, "first", "KES", fixedInstant());
            assertThatThrownBy(() -> rawInsert(id, "second", "KES", fixedInstant()))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        /**
         * Documents current behaviour, not a desired one: the DB has no CHECK on
         * currency, so a row written outside the domain (manual fix, bad
         * migration, future sync bug) is stored happily and then makes every
         * read of that Business throw. If you add a CHECK constraint or make
         * the mapper tolerant, update this test.
         */
        @Test
        void rowWithUnsupportedCurrency_makesFindByIdFail() {
            UUID id = UUID.randomUUID();
            rawInsert(id, "Corrupt", "JPY", fixedInstant());

            assertThatThrownBy(() -> repository.findById(StableId.fromUuid(id)))
                    .isInstanceOf(InvariantViolationException.class);
        }

        @Test
        void rowWithBlankName_makesFindByIdFail() {
            UUID id = UUID.randomUUID();
            rawInsert(id, "   ", "KES", fixedInstant());

            assertThatThrownBy(() -> repository.findById(StableId.fromUuid(id)))
                    .isInstanceOf(InvariantViolationException.class);
        }
    }
}