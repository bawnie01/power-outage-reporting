package com.poweroutage.smspartner.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class PartnerExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PartnerErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<FieldErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "The request contains invalid data.", details, request);
    }

    @ExceptionHandler(InvalidMockScenarioException.class)
    public ResponseEntity<PartnerErrorResponse> handleInvalidScenario(
            InvalidMockScenarioException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_MOCK_SCENARIO",
                exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(PartnerUnavailableException.class)
    public ResponseEntity<PartnerErrorResponse> handleUnavailable(
            PartnerUnavailableException exception,
            HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "SMS_PARTNER_UNAVAILABLE",
                exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(InvalidPartnerCredentialException.class)
    public ResponseEntity<PartnerErrorResponse> handleInvalidCredential(
            InvalidPartnerCredentialException exception,
            HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_PARTNER_CREDENTIAL",
                exception.getMessage(), List.of(), request);
    }

    private ResponseEntity<PartnerErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            List<FieldErrorDetail> details,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(new PartnerErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                code,
                message,
                details,
                request.getRequestURI()));
    }
}
