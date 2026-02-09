package org.adt.volunteerscase.exception;

public class UserAlreadyExistsException extends RuntimeException {
    /**
     * Create a UserAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message describing why the exception was raised
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}