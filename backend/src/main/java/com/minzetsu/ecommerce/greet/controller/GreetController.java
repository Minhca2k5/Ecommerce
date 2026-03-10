package com.minzetsu.ecommerce.greet.controller;

import com.minzetsu.ecommerce.greet.dto.GreetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/greet")
@Tag(name = "Greet", description = "Greeting endpoint")
public class GreetController {

    @Operation(summary = "Get a greeting message")
    @GetMapping
    public ResponseEntity<GreetResponse> greet(
            @RequestParam(required = false, defaultValue = "World") String name
    ) {
        GreetResponse response = GreetResponse.builder()
                .message("Hello, " + name + "!")
                .build();
        return ResponseEntity.ok(response);
    }
}
