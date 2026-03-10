package com.minzetsu.ecommerce.greet;

import com.minzetsu.ecommerce.greet.controller.GreetController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GreetController.class)
@AutoConfigureMockMvc(addFilters = false)
class GreetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void greet_withNoName_returnsDefaultGreeting() throws Exception {
        mockMvc.perform(get("/api/public/greet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, World!"));
    }

    @Test
    void greet_withName_returnsPersonalisedGreeting() throws Exception {
        mockMvc.perform(get("/api/public/greet").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Alice!"));
    }
}
