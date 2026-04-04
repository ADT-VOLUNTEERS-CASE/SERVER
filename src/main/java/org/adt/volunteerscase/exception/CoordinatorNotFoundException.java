package org.adt.volunteerscase.exception;

public class CoordinatorNotFoundException extends RuntimeException {
    public CoordinatorNotFoundException(String message) {
        super(message);
    }
}
