package org.adt.volunteerscase.exception;

public class UserNotCoordinatorException extends RuntimeException {
    public UserNotCoordinatorException(String message) {
        super(message);
    }
}
