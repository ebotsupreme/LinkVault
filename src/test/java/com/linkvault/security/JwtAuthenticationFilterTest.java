package com.linkvault.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private FilterChain filterChain;
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtils, userDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthentication_WhenTokenIsValid() throws Exception {
        // Arrange
        String token = "valid.token.value0123456789";
        String username = "user";

        request.addHeader("Authorization", "Bearer " + token);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            username, "password", List.of()
        );

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils).validateToken(token);
    }

    @Test
    void shouldNotSetAuthentication_WhenTokenIsInvalid() throws Exception {
        // Arrange
        String token = "invalid.token.value";

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtils.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils).validateToken(token);
    }

    @Test
    void shouldNotSetAuthentication_WhenTokenIsEmpty() throws Exception {
        // Arrange
        String token = "";

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtils.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils).validateToken(token);
    }

    @Test
    void shouldNotSetAuthentication_WhenTokenIsNull() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthentication_WhenTokenIsExpired() throws Exception {
        // Arrange
        String token = "expired.token.value";

        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtils.validateToken(token)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthentication_WhenHeaderIsMissing() throws Exception {
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthentication_WhenHeaderHasWrongPrefix() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Token invalid.token.abc ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthentication_WhenTokenIsMalformed() throws Exception {
        // Arrange
        String token = "invalid.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils).validateToken(token);
    }

    @Test
    void shouldNotSetAuthentication_WhenHeaderIsGarbage() throws Exception {
        // Arrange
        request.addHeader("Authorization", "asfg1!$#@A ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }
}
