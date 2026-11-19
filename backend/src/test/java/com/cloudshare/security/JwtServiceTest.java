package com.cloudshare.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiryMinutes", 60L);
        jwtService.init();
    }

    @Test
    void generateAndValidateToken_success() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        String token = jwtService.generateToken(userId, email);
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));

        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void validateToken_invalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_expiredToken() {
        ReflectionTestUtils.setField(jwtService, "expiryMinutes", -10L); // expired
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "expired@example.com");

        assertFalse(jwtService.validateToken(token));
    }
}
