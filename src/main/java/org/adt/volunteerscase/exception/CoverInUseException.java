package org.adt.volunteerscase.exception;

public class CoverInUseException extends RuntimeException {
    public CoverInUseException(String message) {
        super(message);
    }
}
