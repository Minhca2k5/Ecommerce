package com.minzetsu.ecommerce.greeting;

import com.minzetsu.ecommerce.greeting.dto.GreetingResponse;
import com.minzetsu.ecommerce.greeting.service.GreetingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingServiceTest {

    private final GreetingService greetingService = new GreetingService();

    @Test
    void greet_withName_returnsPersonalizedMessage() {
        GreetingResponse response = greetingService.greet("World");
        assertEquals("Hello, World!", response.getMessage());
    }

    @Test
    void greet_withPaddedName_returnsTrimmedPersonalizedMessage() {
        GreetingResponse response = greetingService.greet("  Alice  ");
        assertEquals("Hello, Alice!", response.getMessage());
    }

    @Test
    void greet_withNullName_returnsDefaultMessage() {
        GreetingResponse response = greetingService.greet(null);
        assertEquals("Hello!", response.getMessage());
    }

    @Test
    void greet_withBlankName_returnsDefaultMessage() {
        GreetingResponse response = greetingService.greet("   ");
        assertEquals("Hello!", response.getMessage());
    }
}
