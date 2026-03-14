package com.minzetsu.ecommerce.common.exception;

import lombok.Data;
import lombok.*;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private Timestamp timestamp;
    private int status;
}



