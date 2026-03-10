package com.minzetsu.ecommerce.greeting.service;

import com.minzetsu.ecommerce.greeting.dto.GreetingResponse;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public GreetingResponse greet(String name) {
        String message = (name != null && !name.isBlank())
                ? String.format("Hello, %s!", name.trim())
                : "Hello!";
        return GreetingResponse.builder().message(message).build();
    }
}
