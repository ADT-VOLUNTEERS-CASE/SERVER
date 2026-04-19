package org.adt.volunteerscase.exception;

public class EventCapacityExceededException extends RuntimeException {
    public EventCapacityExceededException(String message) {
        super(message);
    }
}
