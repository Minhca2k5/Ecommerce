package com.minzetsu.ecommerce.greeting.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GreetingResponse {
    private String message;
}
