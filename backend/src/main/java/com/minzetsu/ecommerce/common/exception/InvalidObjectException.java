package com.minzetsu.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidObjectException extends AppException{
    public InvalidObjectException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
