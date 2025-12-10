package com.minzetsu.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends AppException{
    public UnAuthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
