package org.adt.volunteerscase.exception;

import org.adt.volunteerscase.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles a missing user scenario by producing an error response with code "USER_NOT_FOUND".
     *
     * @param ex the thrown UserNotFoundException
     * @return a ResponseEntity containing an ErrorResponse with code "USER_NOT_FOUND" and the exception message, returned with HTTP status 404 (Not Found)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("USER_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle an InvalidPasswordException by producing an error response indicating an invalid password.
     *
     * @param ex the caught InvalidPasswordException; its message is used as the error description
     * @return a ResponseEntity containing an ErrorResponse with code "INVALID_PASSWORD" and the exception message, returned with HTTP 401 Unauthorized
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_PASSWORD", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Produces a 400 Bad Request response for method-argument validation failures.
     *
     * @param ex the MethodArgumentNotValidException thrown when request validation fails
     * @return a ResponseEntity containing an ErrorResponse with code "VALIDATION_ERROR" and the exception message, using HTTP status 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidValidation(MethodArgumentNotValidException ex) {
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Produces a 409 CONFLICT response with an ErrorResponse when a requested user already exists.
     *
     * @param ex the thrown UserAlreadyExistsException whose message is used as the ErrorResponse detail
     * @return a ResponseEntity containing an ErrorResponse with code "USER_ALREADY_EXISTS" and the exception message, with HTTP status 409 (CONFLICT)
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse("USER_ALREADY_EXISTS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Create an ErrorResponse for an expired or invalid refresh token and return it with HTTP 401.
     *
     * @param ex the thrown RefreshTokenException whose message is used as the error detail
     * @return a ResponseEntity containing an ErrorResponse with code "REFRESH_TOKEN_EXPIRED" and the exception message, with HTTP status 401 (UNAUTHORIZED)
     */
    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpired(RefreshTokenException ex) {
        ErrorResponse error = new ErrorResponse("REFRESH_TOKEN_EXPIRED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}