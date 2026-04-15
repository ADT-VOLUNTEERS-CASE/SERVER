package org.adt.volunteerscase.exception;

public class CoverUploadException extends RuntimeException {
    public CoverUploadException(String message) {
        super(message);
    }

    public CoverUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}

