package com.moneytrasnfer.moneytransferservice.exception;

import com.moneytrasnfer.moneytransferservice.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Functional / Business Rejections (HTTP 422, 404, etc.)
    @ExceptionHandler(FunctionalException.class)
    public ResponseEntity<ErrorResponse> handleFunctionalException(FunctionalException ex) {
        // Log as WARN because this is a normal business rule rejection
        log.warn("Functional rule violation [Code: {}]: {}",
                ex.getErrorCode().getCode(), ex.getMessage());
        ErrorResponse error = new ErrorResponse();
        error.setTraceId(UUID.randomUUID().toString());
        error.setStatus(ex.getErrorCode().getHttpStatus().value());
        error.setMessage(ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(error);
    }

    // 2. Technical / Infrastructure Failures (HTTP 503, 500, etc.)
    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ErrorResponse> handleTechnicalException(TechnicalException ex) {
        // Log as ERROR with full stack trace for system/infrastructure failures
        log.error("Technical exception occurred [Code: {}]: {}",
                ex.getErrorCode().getCode(), ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse();
        error.setTraceId(UUID.randomUUID().toString());
        error.setStatus(ex.getErrorCode().getHttpStatus().value());
        error.setMessage(ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.warn("Payload validation failed: {}", errorMessage);

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage(errorMessage);
        error.setTraceId(UUID.randomUUID().toString()); // or your trace ID supplier

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUncaughtException(Exception ex) {
        log.error("Unexpected server error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage("An unexpected error occurred");
        error.setTraceId(UUID.randomUUID().toString());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
