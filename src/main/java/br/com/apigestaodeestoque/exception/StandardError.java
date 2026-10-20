package br.com.apigestaodeestoque.exception;

import java.time.Instant;
import java.util.List;

public record StandardError(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> errors
) {
    public record FieldErrorDetail(String field, String message) {}
}