package com.rhacarys.racharcontas.api.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.rhacarys.racharcontas.domain.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for REST API.
 * Logs all exceptions and returns standardized error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles business exceptions with custom HTTP status codes.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusiness(BusinessException ex, WebRequest request) {
        log.warn("Business exception caught - status: {}, message: {}", ex.getStatus().value(), ex.getMessage());

        ApiError error = new ApiError(ex.getStatus().value(), Instant.now(), ex.getMessage(), null);
        return handleExceptionInternal(ex, error, new HttpHeaders(), ex.getStatus(), request);
    }

    /**
     * Handles validation errors from request body validation.
     * Logs all field validation failures.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<ApiError.Field> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    log.warn("Validation failed - field: {}, message: {}",
                            fieldError.getField(), fieldError.getDefaultMessage());
                    return new ApiError.Field(fieldError.getField(), fieldError.getDefaultMessage());
                })
                .toList();

        log.warn("Request validation failed - {} field errors", fields.size());
        ApiError error = new ApiError(status.value(), Instant.now(), "Validation failed", fields);
        return handleExceptionInternal(ex, error, headers, status, request);
    }
}