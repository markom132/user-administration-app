package com.user_admin.app.exceptions;

/**
 * Custom exception class for handling database-related errors.
 * This exception is thrown when there are issues interacting with the database.
 */
public class DatabaseException extends RuntimeException {

    /**
     * Constructs a new DatabaseException with the specified detail message.
     *
     * @param message the detail message which is saved for later retrieval by the getMessage() method
     */
    public DatabaseException(String message) {
        super(message);
    }
}
