package org.adt.volunteerscase.exception;

public class UserNotFoundException extends RuntimeException {
    /**
     * Constructs a UserNotFoundException with the specified detail message.
     *
     * @param message the detail message describing the missing user
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}