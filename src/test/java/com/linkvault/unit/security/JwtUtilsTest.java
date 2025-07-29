package com.linkvault.unit.security;

import com.linkvault.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {
    private JwtUtils jwtUtils;
    private String token;
    private static final String TEST_USER = "testuser";

    @BeforeEach
    void setUp(){
        String testSecret = "sxy8aEjMvcnPANTL/AuUGOGWUrZl4w2JeP+ztrVca24=";
        // 1 hour
        long testExpiration = 1000L * 60 * 60;
        jwtUtils = new JwtUtils(testSecret, testExpiration);
        this.token = jwtUtils.generateToken(TEST_USER);
    }

    @Test
    void generateToken_ShouldReturnToken() {
        assertNotNull(token);
    }

    @Test
    void generateToken_ShouldReturnTrue_ForValidToken() {
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void generateToken_ShouldReturnFalse_ForInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid.token.here"));
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        assertEquals(TEST_USER, jwtUtils.extractUsername(token));
    }
}
