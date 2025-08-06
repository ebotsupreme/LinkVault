package com.linkvault.controller;

import com.linkvault.constants.apiPaths.AuthEndpoints;
import com.linkvault.dto.AuthResponse;
import com.linkvault.dto.LoginRequest;
import com.linkvault.dto.RegisterRequest;
import com.linkvault.security.JwtUtils;
import com.linkvault.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final JwtUtils jwtUtils;
    private final UserServiceImpl userServiceImpl;

    public AuthController(
        AuthenticationManager authenticationManager,
        JwtUtils jwtUtils,
        UserServiceImpl userServiceImpl
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping(AuthEndpoints.LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            info(log, "Logging in user: {}", loginRequest.getUsername());
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            String token = jwtUtils.generateToken(loginRequest.getUsername());

            info(log, "Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping(AuthEndpoints.REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        userServiceImpl.registerUser(request.getUsername(), request.getPassword());
        return ResponseEntity.ok("User registered successfully");
    }
}
