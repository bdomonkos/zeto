package com.zeto.backend.exception;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Standardised error response body returned by the global exception handler.
 */
@Data
@Builder
public class ApiError {

    /** Human-readable error message. */
    private String message;

    /** HTTP status code. */
    private int status;

    /** Request URI that triggered the error. */
    private String path;

    /** Machine-readable error code (e.g. {@code VALIDATION_ERROR}). */
    private String errorCode;

    /** Timestamp when the error occurred. */
    private Instant timestamp;

    /** Field-level validation errors; {@code null} for non-validation errors. */
    private Map<String, String> errors;
}
