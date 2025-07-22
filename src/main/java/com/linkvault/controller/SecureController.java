package com.linkvault.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
public class SecureController {
    @GetMapping("/test")
    public ResponseEntity<?> test(Authentication authentication) {
        return ResponseEntity.ok("Hello " + authentication.getName() + "! You are authenticated.");
    }
}
