package com.example.coursebe.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
@Service
public class JwtService {
    // Use the same signing key as in AUTH-BE service and Caddy
    private final Key key = Keys.hmacShaKeyFor(
        Base64.getDecoder().decode(System.getenv("JWT_TOKEN"))
    );
    
    public String extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String userIdStr = claims.get("userId", String.class);
            return userIdStr;
        } catch (Exception e) {
            return null;
        }
    }
    
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public String extractEmail(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims != null ? claims.get("email", String.class) : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public String extractRole(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims != null ? claims.get("role", String.class) : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public String extractFullName(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims != null ? claims.get("fullName", String.class) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
