package com.ratibu.common.exception;

/**
 * Thrown when a referenced aggregate or resource does not exist
 * (e.g. creating a Shop for an unknown Business).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
