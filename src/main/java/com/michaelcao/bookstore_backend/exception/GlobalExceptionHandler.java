package com.michaelcao.bookstore_backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest; // Added for more context

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j // Added for logging
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    // Response status is set by the exception annotation itself
    public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException ex, WebRequest request) {
        log.warn("Email already exists conflict: {}", ex.getMessage());
        // You can return a structured error object if preferred
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // *** THÊM HANDLER CHO DuplicateResourceException ***
    @ExceptionHandler(DuplicateResourceException.class)
    // @ResponseStatus đã được đặt trong Exception class
    public ResponseEntity<String> handleDuplicateResource(DuplicateResourceException ex, WebRequest request) {
        log.warn("Duplicate resource conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // *** THÊM HANDLER CHO OperationNotAllowedException ***
    @ExceptionHandler(OperationNotAllowedException.class)
    // @ResponseStatus đã được đặt trong Exception class
    public ResponseEntity<String> handleOperationNotAllowed(OperationNotAllowedException ex, WebRequest request) {
        log.warn("Operation not allowed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // *** THÊM HANDLER CHO InvalidTokenException *** (Từ bước trước)
    @ExceptionHandler(InvalidTokenException.class)
    // @ResponseStatus đã được đặt trong Exception class
    public ResponseEntity<String> handleInvalidToken(InvalidTokenException ex, WebRequest request) {
        log.warn("Invalid token encountered: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    // Catch-all for other unexpected RuntimeExceptions
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleGenericRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Unexpected internal server error: ", ex); // Log the full stack trace
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected internal error occurred. Please contact support.");
    }
}