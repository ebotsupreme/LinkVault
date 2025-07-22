package com.linkvault.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    private final long expirationTime;
    private final SecretKey key;

    public JwtUtils(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long expirationTime
    ) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationTime = expirationTime;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + expirationTime);
        JwtBuilder builder = Jwts.builder()
            .claim("sub", username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key);

        return builder.compact();
    }

    public String extractUsername(String token) {
        JwtParser parser = Jwts.parser()
            .verifyWith(key)
            .build();

        Jws<Claims> jwt = parser.parseSignedClaims(token);

        return jwt.getPayload().get("sub", String.class);
    }

    public boolean validateToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                .verifyWith(key)
                .build();

            parser.parseSignedClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
