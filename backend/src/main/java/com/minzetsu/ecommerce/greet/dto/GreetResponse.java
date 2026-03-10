package com.minzetsu.ecommerce.greet.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GreetResponse {
    private String message;
}
