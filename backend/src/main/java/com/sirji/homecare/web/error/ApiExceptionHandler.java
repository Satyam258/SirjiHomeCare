package com.sirji.homecare.web.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(ServiceAreaException.class)
    ResponseEntity<ApiError> outOfArea(ServiceAreaException exception) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "OUT_OF_SERVICE_AREA", exception.getMessage());
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    ResponseEntity<ApiError> invalidState(InvalidOrderStateException exception) {
        return error(HttpStatus.CONFLICT, "INVALID_ORDER_STATE", exception.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiError> badCredentials() {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is incorrect.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst().map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
            .orElse("Request validation failed.");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> invalidArgument(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ResponseEntity<ApiError> conflict(Exception exception) {
        return error(HttpStatus.CONFLICT, "CONFLICT", "The request conflicts with current data. Refresh and try again.");
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code, message));
    }

    public record ApiError(Instant timestamp, int status, String code, String message) {}
}
