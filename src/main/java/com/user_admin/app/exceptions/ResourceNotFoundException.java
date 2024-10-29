package com.user_admin.app.exceptions;

/**
 * Exception thrown when a requested resource is not found.
 * This is a runtime exception, indicating that the resource
 * requested does not exist in the application.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with specified detail message.
     *
     * @param message the detail message. which is saved for later retrieval
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
