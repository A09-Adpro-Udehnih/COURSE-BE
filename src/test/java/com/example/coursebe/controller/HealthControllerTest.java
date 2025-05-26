package com.example.coursebe.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.coursebe.config.JwtService;
import com.example.coursebe.dto.GlobalResponse;

@ExtendWith(MockitoExtension.class)
public class HealthControllerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private Principal principal;

    @InjectMocks
    private HealthController healthController;

    private String validToken;
    private String validAuthHeader;

    @BeforeEach
    void setUp() {
        validToken = "valid.jwt.token";
        validAuthHeader = "Bearer " + validToken;
    }

    @Test
    @DisplayName("Should return health check message")
    void health() {
        // When
        String result = healthController.health();

        // Then
        assertEquals("Course Service is running", result);
    }

    @Test
    @DisplayName("Should return token info with valid principal and token")
    void getTokenInfo_WithValidPrincipalAndToken() {
        // Given
        String userId = "test-user";
        String email = "test@example.com";
        String role = "ROLE_USER";
        String fullName = "Test User";

        when(principal.getName()).thenReturn(userId);
        when(jwtService.extractEmail(validToken)).thenReturn(email);
        when(jwtService.extractRole(validToken)).thenReturn(role);
        when(jwtService.extractFullName(validToken)).thenReturn(fullName);
        when(jwtService.validateToken(validToken)).thenReturn(true);

        // When
        ResponseEntity<GlobalResponse<Map<String, Object>>> response = 
            healthController.getTokenInfo(principal, validAuthHeader);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Token information retrieved successfully.", response.getBody().getMessage());

        Map<String, Object> tokenInfo = response.getBody().getData();
        assertEquals(userId, tokenInfo.get("userId"));
        assertEquals(principal.getClass().getName(), tokenInfo.get("authenticationType"));
        assertEquals(email, tokenInfo.get("email"));
        assertEquals(role, tokenInfo.get("role"));
        assertEquals(fullName, tokenInfo.get("fullName"));
        assertEquals(true, tokenInfo.get("tokenValid"));
    }

    @Test
    @DisplayName("Should handle token extraction error")
    void getTokenInfo_WithTokenExtractionError() {
        // Given
        String userId = "test-user";
        when(principal.getName()).thenReturn(userId);
        when(jwtService.extractEmail(validToken)).thenThrow(new RuntimeException("Token extraction error"));

        // When
        ResponseEntity<GlobalResponse<Map<String, Object>>> response = 
            healthController.getTokenInfo(principal, validAuthHeader);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Token information retrieved successfully.", response.getBody().getMessage());

        Map<String, Object> tokenInfo = response.getBody().getData();
        assertEquals(userId, tokenInfo.get("userId"));
        assertEquals(principal.getClass().getName(), tokenInfo.get("authenticationType"));
        assertEquals("Token extraction error", tokenInfo.get("tokenError"));
    }

    @Test
    @DisplayName("Should handle invalid auth header format")
    void getTokenInfo_WithInvalidAuthHeader() {
        // Given
        String userId = "test-user";
        String invalidAuthHeader = "InvalidFormat " + validToken;
        when(principal.getName()).thenReturn(userId);

        // When
        ResponseEntity<GlobalResponse<Map<String, Object>>> response = 
            healthController.getTokenInfo(principal, invalidAuthHeader);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Token information retrieved successfully.", response.getBody().getMessage());

        Map<String, Object> tokenInfo = response.getBody().getData();
        assertEquals(userId, tokenInfo.get("userId"));
        assertEquals(principal.getClass().getName(), tokenInfo.get("authenticationType"));
    }

    @Test
    @DisplayName("Should handle null auth header")
    void getTokenInfo_WithNullAuthHeader() {
        // Given
        String userId = "test-user";
        when(principal.getName()).thenReturn(userId);

        // When
        ResponseEntity<GlobalResponse<Map<String, Object>>> response = 
            healthController.getTokenInfo(principal, null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Token information retrieved successfully.", response.getBody().getMessage());

        Map<String, Object> tokenInfo = response.getBody().getData();
        assertEquals(userId, tokenInfo.get("userId"));
        assertEquals(principal.getClass().getName(), tokenInfo.get("authenticationType"));
    }

    @Test
    @DisplayName("Should handle null principal")
    void getTokenInfo_WithNullPrincipal() {
        // When
        ResponseEntity<GlobalResponse<Map<String, Object>>> response = 
            healthController.getTokenInfo(null, validAuthHeader);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Anonymous access - no token information available.", response.getBody().getMessage());

        Map<String, Object> tokenInfo = response.getBody().getData();
        assertEquals("anonymous-user", tokenInfo.get("userId"));
        assertEquals("No authentication provided", tokenInfo.get("message"));
    }
} 