package com.example.ci.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ci.service.GreetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

// @WebMvcTest loads only the web layer, so the real GreetingService isn't a bean
// by default. It has no dependencies of its own, so importing it directly is
// simpler (and cleaner) than mocking it.
@WebMvcTest(GreetingController.class)
@Import(GreetingService.class)
class GreetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void greetingUsesProvidedName() throws Exception {
        mockMvc.perform(get("/api/greeting").param("name", "Asha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, Asha!"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void greetingDefaultsWhenNameMissing() throws Exception {
        mockMvc.perform(get("/api/greeting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, World!"));
    }

    @Test
    void healthReportsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
