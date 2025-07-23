package com.linkvault.LinkControllerTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private ObjectMapper objectMapper;
    private static final String username = "user";

    @BeforeEach
    void setUp() {

    }

    @Test
    void shouldReturnSuccessMessageWhenAuthenticated() throws Exception {
        // Arrange
        String json = String.format("""
            {
                "username": "%s",
                "password": "password"
            }
            """, username);

        // Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);
        String token = jsonNode.get("token").asText();

        mockMvc.perform(get("/api/secure/test")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string(String.format("Hello %s! You are authenticated.", username)));
    }

    @Test
    void shouldReturn401_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/secure/test"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/secure/test")
            .header("Authorization", "Bearer invalid.token.value"))
            .andExpect(status().isUnauthorized());
    }

    private String generateTokenWithDifferentSecret() {
        SecretKey otherKey = Keys.hmacShaKeyFor("invalid-secret-key-12345678901234567890".getBytes());
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 10);

        JwtBuilder builder = Jwts.builder()
            .claim("sub", AuthIntegrationTest.username)
            .issuedAt((now))
            .expiration(expiryDate)
            .signWith(otherKey);

        return builder.compact();
    }

    @Test
    void shouldReturn401_WhenTokenHasInvalidSignature() throws Exception {
        String token = generateTokenWithDifferentSecret();

        mockMvc.perform(get("/api/secure/test")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }
}
