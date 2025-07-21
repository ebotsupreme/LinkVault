package com.linkvault.util;

import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours
    private static final String SECRET = "mysupersecretkeythatshouldbeatleast256bitslong!";

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
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
