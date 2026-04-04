package org.adt.volunteerscase.exception;

public class CoordinatorInUseException extends RuntimeException {
    public CoordinatorInUseException(String message) {
        super(message);
    }
}
