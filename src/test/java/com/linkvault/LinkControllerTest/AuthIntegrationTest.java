package com.linkvault.LinkControllerTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.constants.apiPaths.AuthEndpoints;
import com.linkvault.util.TestConstants;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static final String username = "user";
    @Value("${jwt.secret}")
    private String jwtSecret;

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
        MvcResult result = mockMvc.perform(post(AuthEndpoints.BASE_AUTH + AuthEndpoints.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);
        String token = jsonNode.get("token").asText();

        mockMvc.perform(get(TestConstants.SECURE_TEST_ENDPOINT)
            .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + token))
            .andExpect(status().isOk())
            .andExpect(content().string(String.format("Hello %s! You are authenticated.", username)));
    }

    @Test
    void shouldReturn401_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get(TestConstants.SECURE_TEST_ENDPOINT))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get(TestConstants.SECURE_TEST_ENDPOINT)
            .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + "invalid.token.value"))
            .andExpect(status().isUnauthorized());
    }

    private String generateToken(SecretKey key, Date now, Date expiryDate) {
        JwtBuilder builder = Jwts.builder()
            .claim("sub", AuthIntegrationTest.username)
            .issuedAt((now))
            .expiration(expiryDate)
            .signWith(key);

        return builder.compact();
    }

    @Test
    void shouldReturn401_WhenTokenHasInvalidSignature() throws Exception {
        SecretKey otherKey = Keys.hmacShaKeyFor("invalid-secret-key-12345678901234567890".getBytes());
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 10);

        String token = generateToken(otherKey, now, expiryDate);

        mockMvc.perform(get(TestConstants.SECURE_TEST_ENDPOINT)
            .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + token))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_WhenTokenHasExpired() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Date issuedTenMinutesAgo = new Date(System.currentTimeMillis() - 1000 * 60 * 10);
        Date expiredFiveMinutesAgo = new Date(System.currentTimeMillis() - 1000 * 60 * 5);

        String token = generateToken(key, issuedTenMinutesAgo, expiredFiveMinutesAgo);

        mockMvc.perform(get(TestConstants.SECURE_TEST_ENDPOINT)
            .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + token))
            .andExpect(status().isUnauthorized());
    }
}
