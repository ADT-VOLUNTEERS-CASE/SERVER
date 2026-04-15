package org.adt.volunteerscase.exception;

public class SimultaneouslyCleaningAndWritingCoverException extends RuntimeException {
    public SimultaneouslyCleaningAndWritingCoverException(String message) {
        super(message);
    }
}
