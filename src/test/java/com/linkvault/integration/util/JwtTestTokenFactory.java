package com.linkvault.integration.util;

import com.linkvault.integration.auth.AuthIntegrationTest;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtTestTokenFactory {
    private static final String username = "validUsername";

    public static String buildExpiredToken(String jwtSecret) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Date issuedTenMinutesAgo = new Date(System.currentTimeMillis() - 1000 * 60 * 10);
        Date expiredFiveMinutesAgo = new Date(System.currentTimeMillis() - 1000 * 60 * 5);
        return generateToken(key, issuedTenMinutesAgo, expiredFiveMinutesAgo);
    }

    public static String generateToken(SecretKey key, Date now, Date expiryDate) {
        JwtBuilder builder = Jwts.builder()
            .claim("sub", JwtTestTokenFactory.username)
            .issuedAt((now))
            .expiration(expiryDate)
            .signWith(key);

        return builder.compact();
    }
}
