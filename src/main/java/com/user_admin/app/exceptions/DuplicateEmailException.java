package com.user_admin.app.exceptions;

/**
 * Custom exception class for handling duplicate email errors.
 * This exception is thrown when an attempt is made to register
 * ot update a user with an email that already exists in the database.
 */
public class DuplicateEmailException extends RuntimeException {

    /**
     * Constructs a new DuplicateEmailException with the specified detail message.
     *
     * @param message the detail message which is saved for later retrieval by the getMessage() method
     */
    public DuplicateEmailException(String message) {
        super(message);
    }
}

