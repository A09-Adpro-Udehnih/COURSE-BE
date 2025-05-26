package com.example.coursebe.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HealthCheckControllerTest {

    private final HealthController healthCheckController = new HealthController();

    @Test
    void healthCheck_ShouldReturnOkResponse() {
        String response = healthCheckController.health();

        assertNotNull(response);
        assertEquals("Course Service is running", response);
    }
} 