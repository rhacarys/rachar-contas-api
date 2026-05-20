package com.rhacarys.racharcontas.api.exception;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Integer status,
        Instant timestamp,
        String message,
        List<Field> fields) {
    public record Field(String name, String message) {
    }
}