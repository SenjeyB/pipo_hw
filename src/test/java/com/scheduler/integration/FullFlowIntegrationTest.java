package com.scheduler.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FullFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCompleteFullFlow() throws Exception {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());

        RegisterRequest registerOwner = new RegisterRequest(
                "owner" + uniqueSuffix,
                "owner" + uniqueSuffix + "@test.com",
                "password123"
        );

        MvcResult ownerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerOwner)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value(registerOwner.getUsername()))
                .andReturn();

        AuthResponse ownerAuth = objectMapper.readValue(
                ownerResult.getResponse().getContentAsString(), AuthResponse.class);
        String ownerToken = ownerAuth.getToken();

        CreateIntervalRequest createInterval = new CreateIntervalRequest(
                "Meeting",
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(11).withMinute(0)
        );

        MvcResult intervalResult = mockMvc.perform(post("/api/intervals")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createInterval)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Meeting"))
                .andExpect(jsonPath("$.booked").value(false))
                .andReturn();

        IntervalResponse createdInterval = objectMapper.readValue(
                intervalResult.getResponse().getContentAsString(), IntervalResponse.class);

        mockMvc.perform(get("/api/intervals/my")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(createdInterval.getId()));

        mockMvc.perform(get("/api/intervals/available")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + createdInterval.getId() + "')]").exists());

        RegisterRequest registerBooker = new RegisterRequest(
                "booker" + uniqueSuffix,
                "booker" + uniqueSuffix + "@test.com",
                "password123"
        );

        MvcResult bookerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBooker)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse bookerAuth = objectMapper.readValue(
                bookerResult.getResponse().getContentAsString(), AuthResponse.class);
        String bookerToken = bookerAuth.getToken();

        mockMvc.perform(post("/api/intervals/" + createdInterval.getId() + "/book")
                        .header("Authorization", "Bearer " + bookerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booked").value(true))
                .andExpect(jsonPath("$.bookedBy").value(bookerAuth.getUser().getId()));

        mockMvc.perform(post("/api/intervals/" + createdInterval.getId() + "/book")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/intervals/" + createdInterval.getId() + "/book")
                        .header("Authorization", "Bearer " + bookerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booked").value(false));

        UpdateIntervalRequest updateRequest = new UpdateIntervalRequest(
                "Updated Meeting",
                createInterval.getStartTime().plusHours(1),
                createInterval.getEndTime().plusHours(1)
        );

        mockMvc.perform(put("/api/intervals/" + createdInterval.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Meeting"));

        mockMvc.perform(delete("/api/intervals/" + createdInterval.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/intervals/" + createdInterval.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAccessHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void shouldAccessMetricsEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
