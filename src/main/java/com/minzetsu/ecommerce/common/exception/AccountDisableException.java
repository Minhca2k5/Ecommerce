package com.minzetsu.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class AccountDisableException extends AppException {

    public AccountDisableException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
