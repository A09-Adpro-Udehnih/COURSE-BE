package com.example.coursebe.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.coursebe.dto.GlobalResponse;

@RestController
@RequestMapping("/")
public class HealthController {
    @GetMapping
    public String health() {
        return "Course Service is running";
    }
    
    @GetMapping("/token-info")
    public ResponseEntity<GlobalResponse<Map<String, Object>>> getTokenInfo(Principal principal) {
        Map<String, Object> tokenInfo = new HashMap<>();
        
        if (principal != null) {
            // Extract actual data from JWT token
            tokenInfo.put("userId", principal.getName());
            tokenInfo.put("authenticationType", principal.getClass().getName());
            
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