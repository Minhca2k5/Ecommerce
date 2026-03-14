package com.minzetsu.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialException extends AppException{
    public InvalidCredentialException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}



