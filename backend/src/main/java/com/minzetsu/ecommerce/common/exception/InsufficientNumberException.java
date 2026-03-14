package com.minzetsu.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class InsufficientNumberException extends AppException{
    public InsufficientNumberException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}



