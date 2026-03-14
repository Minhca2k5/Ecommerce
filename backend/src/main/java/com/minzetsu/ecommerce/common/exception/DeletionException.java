package com.minzetsu.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class DeletionException extends AppException {
    public DeletionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }


}



