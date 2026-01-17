package org.adt.volunteerscase.exception;

public class RefreshTokenException extends RuntimeException {
    /**
     * Create a RefreshTokenException with the specified detail message.
     *
     * @param message the detail message describing the refresh token error
     */
    public RefreshTokenException(String message) {
        super(message);
    }
}