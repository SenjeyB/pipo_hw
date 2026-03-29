package com.scheduler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.dto.CreateIntervalRequest;
import com.scheduler.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntervalControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldRejectUnauthorizedIntervalCreation() throws Exception {
        CreateIntervalRequest request = new CreateIntervalRequest(
                "Test Interval",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/api/intervals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthorizedMyIntervals() throws Exception {
        mockMvc.perform(get("/api/intervals/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthorizedAvailableIntervals() throws Exception {
        mockMvc.perform(get("/api/intervals/available"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidTimeRange() throws Exception {
        String token = jwtService.generateToken("550e8400-e29b-41d4-a716-446655440000");
        CreateIntervalRequest request = new CreateIntervalRequest(
                "Test Interval",
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(1)
        );

        mockMvc.perform(post("/api/intervals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingTitle() throws Exception {
        String token = jwtService.generateToken("550e8400-e29b-41d4-a716-446655440000");
        CreateIntervalRequest request = new CreateIntervalRequest(
                null,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        mockMvc.perform(post("/api/intervals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
