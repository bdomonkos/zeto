package com.zeto.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Turns exceptions into a consistent {@link ApiError} response body. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@code @Valid} annotation failures; maps field names to messages.
     *
     * @param ex      the validation exception
     * @param request current HTTP request
     * @return {@link ApiError} with code {@code VALIDATION_ERROR}, HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation failed: {}", errors);

        return ApiError.builder()
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .status(400)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .errors(errors)
                .build();
    }

    /**
     * @param ex      exception from business-rule validation
     * @param request current HTTP request
     * @return {@link ApiError} with code {@code BAD_REQUEST}, HTTP 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad request: {}", ex.getMessage());

        return ApiError.builder()
                .message(ex.getMessage())
                .errorCode("BAD_REQUEST")
                .status(400)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * @param ex      unexpected exception
     * @param request current HTTP request
     * @return {@link ApiError} with code {@code INTERNAL_ERROR}, HTTP 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);

        return ApiError.builder()
                .message("Internal server error")
                .errorCode("INTERNAL_ERROR")
                .status(500)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
    }
}
