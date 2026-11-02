package com.ratibu.common.exception;

/**
 * Thrown when a domain invariant is violated (e.g. blank business name,
 * unsupported currency code). Domain code raises this rather than returning
 * null or a boolean so callers cannot ignore the failure.
 */
public class InvariantViolationException extends RuntimeException {

    public InvariantViolationException(String message) {
        super(message);
    }
}
