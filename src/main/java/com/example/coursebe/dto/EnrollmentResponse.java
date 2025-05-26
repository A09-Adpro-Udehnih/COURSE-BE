package com.example.coursebe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    private UUID courseId;
    private String courseName;
    private String courseDescription;
    private String courseTutor;
    private UUID enrollmentId;
    private LocalDateTime enrollmentDate;
}
