package org.adt.volunteerscase.exception;

public class InvalidPasswordException extends RuntimeException {
    /**
     * Constructs a new InvalidPasswordException with the specified detail message.
     *
     * @param message the detail message explaining why the password is considered invalid
     */
    public InvalidPasswordException(String message) {
        super(message);
    }
}