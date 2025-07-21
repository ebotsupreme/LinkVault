package com.linkvault.controller;

import com.linkvault.constants.apiPaths.AuthEndpoints;
import com.linkvault.dto.AuthResponse;
import com.linkvault.dto.LoginRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.linkvault.util.LogUtils.info;

@Validated
@Slf4j
@RestController
@RequestMapping(AuthEndpoints.BASE_AUTH)
public class AuthController {
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(AuthEndpoints.LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            info(log, "Logging in user: {}", loginRequest.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            String token = "dummy-token";

            info(log, "Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

    }
}
