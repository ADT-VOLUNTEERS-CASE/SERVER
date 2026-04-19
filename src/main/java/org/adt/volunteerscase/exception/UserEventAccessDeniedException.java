package org.adt.volunteerscase.exception;

public class UserEventAccessDeniedException extends RuntimeException {
    public UserEventAccessDeniedException(String message) {
        super(message);
    }
}
