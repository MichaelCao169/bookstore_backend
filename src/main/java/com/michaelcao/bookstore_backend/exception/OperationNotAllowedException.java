package com.michaelcao.bookstore_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class OperationNotAllowedException extends RuntimeException {
    
    public OperationNotAllowedException(String message) {
        super(message);
    }
    
    public OperationNotAllowedException(String resourceName, String operation, String reason) {
        super(String.format("Cannot %s %s: %s", operation, resourceName, reason));
    }
}