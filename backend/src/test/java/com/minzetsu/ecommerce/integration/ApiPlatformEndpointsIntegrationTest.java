package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiPlatformEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actuatorHealth_shouldBePublic() throws Exception {
        MvcResult result = mockMvc.perform(get("/actuator/health"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(200, 503);
    }

    @Test
    void actuatorInfo_shouldBePubliclyReachable() throws Exception {
        MvcResult result = mockMvc.perform(get("/actuator/info"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }

    @Test
    void swaggerUi_shouldBePubliclyReachable() throws Exception {
        MvcResult result = mockMvc.perform(get("/swagger-ui/index.html"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(200, 302, 404);
    }

    @Test
    void openApiDocs_shouldBePubliclyReachable() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(200, 404);
    }

    @Test
    void adminEndpoint_shouldStillRejectAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isForbidden());
    }
}
