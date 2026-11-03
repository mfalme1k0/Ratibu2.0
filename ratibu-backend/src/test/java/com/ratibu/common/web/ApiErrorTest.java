package com.ratibu.common.web;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiErrorTest {

    @Test
    void of_carriesEveryFieldAndStampsTheCurrentTime() {
        Instant before = Instant.now();

        ApiError error = ApiError.of(400, "Validation Failed", "Request validation failed",
                "/api/v1/businesses", List.of("name: name is required"));

        Instant after = Instant.now();
        assertEquals(400, error.status());
        assertEquals("Validation Failed", error.error());
        assertEquals("Request validation failed", error.message());
        assertEquals("/api/v1/businesses", error.path());
        assertEquals(List.of("name: name is required"), error.details());
        assertFalse(error.timestamp().isBefore(before));
        assertFalse(error.timestamp().isAfter(after));
    }

    @Test
    void of_withNullDetails_usesAnEmptyList() {
        ApiError error = ApiError.of(400, "Bad Request", "boom", "/x", null);

        assertTrue(error.details().isEmpty());
    }
}