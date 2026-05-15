package com.rhacarys.contaconjunta.api.exception;

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

import com.rhacarys.contaconjunta.domain.exception.BusinessException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusiness(BusinessException ex, WebRequest request) {
        ApiError error = new ApiError(ex.getStatus().value(), Instant.now(), ex.getMessage(), null);
        return handleExceptionInternal(ex, error, new HttpHeaders(), ex.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<ApiError.Field> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ApiError.Field(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        ApiError error = new ApiError(status.value(), Instant.now(), "Validation failed", fields);
        return handleExceptionInternal(ex, error, headers, status, request);
    }
}