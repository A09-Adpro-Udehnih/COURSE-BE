package com.example.coursebe.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.coursebe.config.JwtService;
import com.example.coursebe.dto.GlobalResponse;

@RestController
@RequestMapping("/")
public class HealthController {
    
    @Autowired
    private JwtService jwtService;
    
    @GetMapping
    public String health() {
        return "Course Service is running";
    }
    
    @GetMapping("token-info")
    public ResponseEntity<GlobalResponse<Map<String, Object>>> getTokenInfo(
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> tokenInfo = new HashMap<>();
        
        if (principal != null) {
            // Extract data from Principal (authentication context)
            tokenInfo.put("userId", principal.getName());
            tokenInfo.put("authenticationType", principal.getClass().getName());
            
            // If we have an auth header, try to extract more info directly from the token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    // Add additional info from the token
                    tokenInfo.put("email", jwtService.extractEmail(token));
                    tokenInfo.put("role", jwtService.extractRole(token));
                    tokenInfo.put("fullName", jwtService.extractFullName(token));
                    tokenInfo.put("tokenValid", jwtService.validateToken(token));
                } catch (Exception e) {
                    tokenInfo.put("tokenError", e.getMessage());
                }
            }
            
            return ResponseEntity.ok(GlobalResponse.<Map<String, Object>>builder()
                    .code(HttpStatus.OK)
                    .success(true)
                    .message("Token information retrieved successfully.")
                    .data(tokenInfo)
                    .build());
        } else {
            // For testing when no authentication is provided
            tokenInfo.put("userId", "anonymous-user");
            tokenInfo.put("message", "No authentication provided");
            
            return ResponseEntity.ok(GlobalResponse.<Map<String, Object>>builder()
                    .code(HttpStatus.OK)
                    .success(true)
                    .message("Anonymous access - no token information available.")
                    .data(tokenInfo)
                    .build());
        }
    }
}