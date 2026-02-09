package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiErrorContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authLoginValidationError_shouldUseStandardErrorResponse() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unauthorizedRequest_shouldContainRequestIdHeader() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void requestIdHeader_shouldBeEchoedWhenProvided() throws Exception {
        String requestId = "it-fixed-request-id";

        mockMvc.perform(get("/api/public/categories")
                        .header("X-Request-Id", requestId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", requestId));
    }

    @Test
    void requestIdHeader_shouldBeGeneratedWhenMissing() throws Exception {
        String generated = mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeader("X-Request-Id");

        assertThat(generated).isNotBlank();
        assertThat(Pattern.compile("^[0-9a-fA-F-]{36}$").matcher(generated).matches()).isTrue();
    }
}
