package br.com.apigestaodeestoque.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Trata exceções de regra de negócio (ex: estoque insuficiente, e-mail duplicado)
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<StandardError> handleRegraNegocio(RegraNegocioException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Regra de Negócio Violada",
                e.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(status).body(err);
    }

    // 2. Trata erros de validação dos DTOs (@NotBlank, @Min, @Email, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY; // 422 ou 400

        List<StandardError.FieldErrorDetail> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new StandardError.FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Erro de Validação",
                "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(err);
    }
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException e,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Corpo da requisição inválido",
                e.getMostSpecificCause().getMessage(), // Revela o campo exato que causou o erro
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(status).body(err);
    }
}