package com.example.coursebe.dto.tutorapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TutorApplicationResponse {
    private UUID tutorApplicationId;
    private UUID studentId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
