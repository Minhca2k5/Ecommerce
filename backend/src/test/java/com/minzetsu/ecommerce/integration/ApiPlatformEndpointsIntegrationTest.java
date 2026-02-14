package com.minzetsu.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiPlatformEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actuatorHealth_shouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorMetrics_shouldRejectAnonymousAfterHardening() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldStillRejectAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isForbidden());
    }
}
