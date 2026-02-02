package com.minzetsu.ecommerce.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.Timestamp;
import java.util.Optional;

@ControllerAdvice
@Slf4j
public class GeneralExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                new Timestamp(System.currentTimeMillis()),
                ex.getStatus().value()
        );
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid username or password",
                new Timestamp(System.currentTimeMillis()),
                401
        );
        return ResponseEntity.status(401).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Optional<FieldError> firstError = ex.getBindingResult().getFieldErrors().stream().findFirst();
        String message = firstError.map(FieldError::getDefaultMessage).orElse("Validation failed");
        ErrorResponse errorResponse = new ErrorResponse(
                message,
                new Timestamp(System.currentTimeMillis()),
                400
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Unhandled exception", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                "Internal server error",
                new Timestamp(System.currentTimeMillis()),
                500
        );
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
