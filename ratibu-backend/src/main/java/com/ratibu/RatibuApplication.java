package com.ratibu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. See ARCHITECTURE.md at the project root for the package
 * structure and its mapping to ADR-001 through ADR-014.
 */
@SpringBootApplication
public class RatibuApplication {

    public static void main(String[] args) {
        SpringApplication.run(RatibuApplication.class, args);
    }
}