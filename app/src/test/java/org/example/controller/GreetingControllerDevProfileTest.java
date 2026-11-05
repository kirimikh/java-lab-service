package org.example.controller;

import org.example.config.GreetingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GreetingController.class)
@EnableConfigurationProperties(GreetingProperties.class)
@ActiveProfiles("dev")
class GreetingControllerDevProfileTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void greetsByName() throws Exception {
        mockMvc.perform(get("/api/greeting").param("name", "lab"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("[dev] Hello, lab!"));
    }

    @Test
    void greetsWorldByDefault() throws Exception {
        mockMvc.perform(get("/api/greeting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("[dev] Hello, world!"));
    }
}
