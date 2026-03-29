package com.pipo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("super-secret-key-change-me-to-at-least-32-characters", 259200000L);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String token = jwtService.generateToken(userId);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals(userId, jwtService.getUserIdFromToken(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }

    @Test
    void shouldRejectNullToken() {
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService shortLivedService = new JwtService(
                "super-secret-key-change-me-to-at-least-32-characters", -1000L);
        String token = shortLivedService.generateToken("test-user");
        assertFalse(shortLivedService.validateToken(token));
    }
}
