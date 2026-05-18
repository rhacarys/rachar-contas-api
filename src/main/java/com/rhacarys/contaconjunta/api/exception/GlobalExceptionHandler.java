package com.rhacarys.contaconjunta.api.exception;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.rhacarys.contaconjunta.domain.exception.BusinessException;

/**
 * Global exception handler for REST API.
 * Logs all exceptions and returns standardized error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles business exceptions with custom HTTP status codes.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusiness(BusinessException ex, WebRequest request) {
        logger.warn("Business exception caught - status: {}, message: {}", ex.getStatus().value(), ex.getMessage());
        
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
                    logger.warn("Validation failed - field: {}, message: {}", 
                        fieldError.getField(), fieldError.getDefaultMessage());
                    return new ApiError.Field(fieldError.getField(), fieldError.getDefaultMessage());
                })
                .toList();

        logger.warn("Request validation failed - {} field errors", fields.size());
        ApiError error = new ApiError(status.value(), Instant.now(), "Validation failed", fields);
        return handleExceptionInternal(ex, error, headers, status, request);
    }
}