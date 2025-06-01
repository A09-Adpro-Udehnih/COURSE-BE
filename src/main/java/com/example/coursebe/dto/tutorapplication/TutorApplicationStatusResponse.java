package com.example.coursebe.dto.tutorapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TutorApplicationStatusResponse {
    private String status;
    private UUID tutorApplicationId;
    private String message;
}
