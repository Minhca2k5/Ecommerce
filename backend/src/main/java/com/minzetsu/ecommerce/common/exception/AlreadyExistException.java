package com.minzetsu.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistException extends AppException {
    public AlreadyExistException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}



