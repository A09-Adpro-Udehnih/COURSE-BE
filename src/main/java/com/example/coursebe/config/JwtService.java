package com.example.coursebe.config;

import org.springframework.stereotype.Service;

@Service
public class JwtService {
    // Simplified implementation for testing
    
    public String extractUserIdFromToken(String token) {
        // In a real implementation, this would decode the JWT and extract the user ID
        // For now, just return a placeholder value for testing
        return "test-user-id";
    }

    public boolean validateToken(String token) {
        // In a real implementation, this would validate the JWT signature
        // For now, consider all tokens valid for testing
        return true;
    }
}
