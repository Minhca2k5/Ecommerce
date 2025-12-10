package com.minzetsu.ecommerce.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.Timestamp;

@ControllerAdvice
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Internal server error: " + ex.getMessage(),
                new Timestamp(System.currentTimeMillis()),
                500
        );
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
