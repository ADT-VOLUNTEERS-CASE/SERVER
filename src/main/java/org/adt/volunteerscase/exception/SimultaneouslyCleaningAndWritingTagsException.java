package org.adt.volunteerscase.exception;

public class SimultaneouslyCleaningAndWritingTagsException extends RuntimeException {
    public SimultaneouslyCleaningAndWritingTagsException(String message) {
        super(message);
    }
}
