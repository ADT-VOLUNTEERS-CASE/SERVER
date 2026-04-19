package org.adt.volunteerscase.exception;

public class UserEventAlreadyExistsException extends RuntimeException {
    public UserEventAlreadyExistsException(String message) {
        super(message);
    }
}
