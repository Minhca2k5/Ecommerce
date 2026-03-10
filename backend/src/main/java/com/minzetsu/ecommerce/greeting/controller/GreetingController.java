package com.minzetsu.ecommerce.greeting.controller;

import com.minzetsu.ecommerce.greeting.dto.GreetingResponse;
import com.minzetsu.ecommerce.greeting.service.GreetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/greeting")
@RequiredArgsConstructor
@Validated
@Tag(name = "Greeting", description = "Greeting endpoint for user interaction")
public class GreetingController {

    private final GreetingService greetingService;

    @Operation(summary = "Get a greeting message")
    @GetMapping
    public ResponseEntity<GreetingResponse> greet(
            @RequestParam(required = false) @Size(max = 100) String name) {
        return ResponseEntity.ok(greetingService.greet(name));
    }
}
