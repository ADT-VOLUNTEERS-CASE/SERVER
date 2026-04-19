package org.adt.volunteerscase.exception;

public class UserEventStateConflictException extends RuntimeException {
    public UserEventStateConflictException(String message) {
        super(message);
    }
}
